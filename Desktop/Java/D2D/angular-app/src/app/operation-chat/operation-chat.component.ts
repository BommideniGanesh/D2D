import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatMessage } from '../services/chat.service';
import { StorageService } from '../services/storage.service';
import { Subscription } from 'rxjs';

const OPERATOR_ID = 'OPERATION_SUPPORT';

@Component({
  selector: 'app-operation-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './operation-chat.component.html',
  styleUrl: './operation-chat.component.css'
})
export class OperationChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('chatBody') private chatBody!: ElementRef;

  contacts: string[] = [];
  selectedContact: string | null = null;
  messages: ChatMessage[] = [];
  newMessage: string = '';
  isConnected: boolean = false;
  isLoadingContacts: boolean = true;
  isLoadingMessages: boolean = false;

  private messageSubscription!: Subscription;

  constructor(
    private chatService: ChatService,
    private storageService: StorageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chatService.connect(undefined, true);
    this.isConnected = true;

    // Load all users who have chatted with OPERATION_SUPPORT
    this.chatService.getRecentContacts(OPERATOR_ID).subscribe({
      next: (contacts) => {
        this.contacts = contacts.filter(c => c !== OPERATOR_ID);
        this.isLoadingContacts = false;
        this.cdr.detectChanges();
      },
      error: () => { this.isLoadingContacts = false; this.cdr.detectChanges(); }
    });

    // Listen for incoming messages from any user
    this.messageSubscription = this.chatService.getMessages().subscribe((msg) => {
      if (!this.contacts.includes(msg.senderId)) {
        this.contacts = [msg.senderId, ...this.contacts];
      }
      if (this.selectedContact === msg.senderId) {
        this.messages = [...this.messages, msg];
      }
      this.cdr.detectChanges();
    });
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  ngOnDestroy(): void {
    if (this.messageSubscription) this.messageSubscription.unsubscribe();
    this.chatService.disconnect();
  }

  selectContact(userId: string): void {
    this.selectedContact = userId;
    this.isLoadingMessages = true;
    this.messages = [];

    this.chatService.getChatHistory(OPERATOR_ID, userId).subscribe({
      next: (history) => {
        this.messages = history;
        this.isLoadingMessages = false;
        this.cdr.detectChanges();
      },
      error: () => { this.isLoadingMessages = false; this.cdr.detectChanges(); }
    });
  }

  sendReply(): void {
    if (!this.newMessage.trim() || !this.selectedContact) return;

    const msg: ChatMessage = {
      senderId: OPERATOR_ID,
      receiverId: this.selectedContact,
      content: this.newMessage.trim()
    };

    this.messages.push({ ...msg, timestamp: new Date().toISOString() });
    this.chatService.sendMessage(msg);
    this.newMessage = '';
  }

  getInitial(userId: string): string {
    return userId ? userId.charAt(0).toUpperCase() : '?';
  }

  private scrollToBottom(): void {
    try {
      this.chatBody.nativeElement.scrollTop = this.chatBody.nativeElement.scrollHeight;
    } catch (e) {}
  }
}
