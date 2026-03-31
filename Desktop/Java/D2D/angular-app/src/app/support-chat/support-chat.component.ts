import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatMessage } from '../services/chat.service';
import { StorageService } from '../services/storage.service';
import { Subscription } from 'rxjs';

const OPERATOR_ID = 'OPERATION_SUPPORT';

@Component({
  selector: 'app-support-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './support-chat.component.html',
  styleUrl: './support-chat.component.css'
})
export class SupportChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('chatBody') private chatBody!: ElementRef;

  messages: ChatMessage[] = [];
  newMessage: string = '';
  currentUserId: string = '';
  isConnected: boolean = false;
  isLoading: boolean = true;

  private messageSubscription!: Subscription;

  constructor(
    private chatService: ChatService,
    private storageService: StorageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.storageService.getUsername() || 'guest';

    this.chatService.connect(this.currentUserId);
    this.isConnected = true;

    // Load history first
    this.chatService.getChatHistory(this.currentUserId, OPERATOR_ID).subscribe({
      next: (history) => {
        this.messages = history;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.isLoading = false; this.cdr.detectChanges(); }
    });

    // Subscribe to live incoming messages
    this.messageSubscription = this.chatService.getMessages().subscribe((msg) => {
      if (msg.senderId === OPERATOR_ID) {
        this.messages = [...this.messages, msg];
        this.cdr.detectChanges();
      }
    });
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  ngOnDestroy(): void {
    if (this.messageSubscription) this.messageSubscription.unsubscribe();
    this.chatService.disconnect();
  }

  sendMessage(): void {
    if (!this.newMessage.trim()) return;

    const msg: ChatMessage = {
      senderId: this.currentUserId,
      receiverId: OPERATOR_ID,
      content: this.newMessage.trim()
    };

    this.messages.push({ ...msg, timestamp: new Date().toISOString() });
    this.chatService.sendMessage(msg);
    this.newMessage = '';
  }

  private scrollToBottom(): void {
    try {
      this.chatBody.nativeElement.scrollTop = this.chatBody.nativeElement.scrollHeight;
    } catch (e) {}
  }
}
