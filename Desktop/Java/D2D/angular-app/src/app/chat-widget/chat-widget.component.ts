import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatMessage } from '../services/chat.service';
import { StorageService } from '../services/storage.service';
import { Subscription } from 'rxjs';

const OPERATOR_ID = 'OPERATION_SUPPORT';

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './chat-widget.component.html',
  styleUrl: './chat-widget.component.css'
})
export class ChatWidgetComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('chatBody') chatBody?: ElementRef;

  // Role detection
  isAdmin = false;
  isLoggedIn = false;

  // UI state: 'closed' | 'connect' | 'chat'
  panelState: 'closed' | 'connect' | 'chat' | 'adminList' | 'adminChat' = 'closed';

  // Chat data
  messages: ChatMessage[] = [];
  newMessage = '';
  displayName = '';
  isConnected = false;
  currentUserId = '';
  hasNewMessage = false;

  // Admin
  contacts: string[] = [];
  selectedContact = '';

  private sub?: Subscription;

  constructor(
    private chatService: ChatService, 
    private storageService: StorageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.storageService.isLoggedIn();
    const user = this.storageService.getUser();
    this.currentUserId = this.storageService.getUsername() ?? '';

    if (user?.roles) {
      this.isAdmin = Array.isArray(user.roles) &&
        (user.roles.includes('ADMIN') || user.roles.includes('OPERATION'));
    }

    // Admins connect right away to catch incoming messages
    if (this.isAdmin) {
      this.chatService.connect(this.currentUserId, true);
      this.sub = this.chatService.getMessages().subscribe(msg => {
        if (!this.contacts.includes(msg.senderId)) {
          this.contacts = [msg.senderId, ...this.contacts];
          this.hasNewMessage = true;
        }
        if (this.selectedContact === msg.senderId) {
          this.messages = [...this.messages, msg];
        }
        this.cdr.detectChanges();
      });
    }
  }

  ngAfterViewChecked(): void {
    try {
      if (this.chatBody) {
        this.chatBody.nativeElement.scrollTop = this.chatBody.nativeElement.scrollHeight;
      }
    } catch { /* ignore */ }
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.chatService.disconnect();
  }

  // ── FAB click ──────────────────────────────────────────────────────────────
  onFabClick(): void {
    if (this.isAdmin) {
      if (this.panelState === 'closed' || this.panelState === 'adminChat' || this.panelState === 'adminList') {
        this.panelState = this.panelState === 'closed' ? 'adminList' : 'closed';
        this.hasNewMessage = false;
        if (this.panelState === 'adminList') this.loadAdminContacts();
      }
    } else {
      if (this.isConnected) {
        const next = this.panelState === 'chat' ? 'closed' : 'chat';
        this.panelState = next;
        if (next === 'chat') {
          // Reload history to catch any messages received while panel was closed
          this.loadHistory();
        }
      } else {
        this.panelState = this.panelState === 'closed' ? 'connect' : 'closed';
      }
    }
  }

  close(): void { this.panelState = 'closed'; }

  // ── User actions ───────────────────────────────────────────────────────────
  connectUser(): void {
    if (!this.displayName.trim()) return;
    this.currentUserId = this.displayName.trim();
    this.isConnected = true;
    this.chatService.connect(this.currentUserId);

    this.loadHistory();

    this.sub = this.chatService.getMessages().subscribe(msg => {
      // Replace optimistic local message with server-confirmed version, or append new
      const localIdx = this.messages.findIndex(
        m => !m.id && m.content === msg.content && m.senderId === msg.senderId
      );
      if (localIdx !== -1) {
        const updated = [...this.messages];
        updated[localIdx] = msg;
        this.messages = updated;
      } else if (!this.messages.some(m => m.id && m.id === msg.id)) {
        this.messages = [...this.messages, msg];
      }
      this.cdr.detectChanges();
    });

    this.panelState = 'chat';
  }

  sendUserMsg(): void {
    const text = this.newMessage.trim();
    if (!text) return;
    const msg: ChatMessage = { senderId: this.currentUserId, receiverId: OPERATOR_ID, content: text };
    this.messages = [...this.messages, { ...msg, timestamp: new Date().toISOString() }];
    this.chatService.sendMessage(msg);
    this.newMessage = '';
    this.cdr.detectChanges();
  }

  private loadHistory(): void {
    this.chatService.getChatHistory(this.currentUserId, OPERATOR_ID).subscribe({
      next: h => {
        // Merge: keep any locally pushed messages not yet confirmed by server
        const historyIds = new Set(h.map(m => m.id).filter(Boolean));
        const pending = this.messages.filter(m => !m.id || !historyIds.has(m.id));
        this.messages = [...h, ...pending];
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  // ── Admin actions ──────────────────────────────────────────────────────────
  loadAdminContacts(): void {
    this.chatService.getRecentContacts(OPERATOR_ID).subscribe({
      next: c => {
        const merged = [...new Set([...c, ...this.contacts])].filter(x => x !== OPERATOR_ID);
        this.contacts = merged;
        this.cdr.detectChanges();
      },
      error: () => { this.cdr.detectChanges(); }
    });
  }

  selectContact(userId: string): void {
    this.selectedContact = userId;
    this.panelState = 'adminChat';
    this.messages = [];
    this.chatService.getChatHistory(OPERATOR_ID, userId).subscribe({
      next: h => { this.messages = h; this.cdr.detectChanges(); },
      error: () => { this.cdr.detectChanges(); }
    });
  }

  sendAdminMsg(): void {
    const text = this.newMessage.trim();
    if (!text || !this.selectedContact) return;
    const msg: ChatMessage = { senderId: OPERATOR_ID, receiverId: this.selectedContact, content: text };
    this.messages = [...this.messages, { ...msg, timestamp: new Date().toISOString() }];
    this.chatService.sendMessage(msg);
    this.newMessage = '';
  }

  backToList(): void {
    this.panelState = 'adminList';
    this.selectedContact = '';
    this.messages = [];
  }

  initial(id: string): string { return id ? id.charAt(0).toUpperCase() : '?'; }
}
