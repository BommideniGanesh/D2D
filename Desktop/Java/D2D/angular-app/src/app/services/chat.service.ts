import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { HttpClient } from '@angular/common/http';
import { StorageService } from './storage.service';

export interface ChatMessage {
  id?: number;
  senderId: string;
  receiverId: string;
  content: string;
  timestamp?: string;
  status?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private stompClient: Client;
  private messageSubject = new Subject<ChatMessage>();
  private API_URL = 'http://localhost:8080/api/chat';
  private pendingMessages: ChatMessage[] = [];

  constructor(private http: HttpClient, private storageService: StorageService) {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      debug: (_str) => {
        // console.log(_str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });


    this.stompClient.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };
  }

  connect(guestUsername?: string, listenToSupportTopic: boolean = false) {
    const headers: any = {};
    if (guestUsername) {
      // Always use guest identity when a username is provided.
      // If we also sent JWT, Spring would authenticate via JWT and the
      // WebSocket principal would become the JWT username — which would
      // NOT match the guestUsername used as senderId/receiverId in messages,
      // causing admin replies to never reach the user.
      headers['Guest-User'] = guestUsername;
    } else {
      const token = this.storageService.getToken();
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
    }
    
    this.stompClient.connectHeaders = headers;
    
    this.stompClient.onConnect = (_frame) => {
      // The backend sends messages to the specific user's queue
      this.stompClient.subscribe('/user/queue/messages', (message: Message) => {
        const chatMsg: ChatMessage = JSON.parse(message.body);
        this.messageSubject.next(chatMsg);
      });

      if (listenToSupportTopic) {
        this.stompClient.subscribe('/topic/support', (message: Message) => {
          const chatMsg: ChatMessage = JSON.parse(message.body);
          this.messageSubject.next(chatMsg);
        });
      }

      // Flush messages queued before connection was ready
      this.pendingMessages.forEach(msg => {
        this.stompClient.publish({
          destination: '/app/chat.sendMessage',
          body: JSON.stringify(msg)
        });
      });
      this.pendingMessages = [];
    };

    if (!this.stompClient.active) {
      this.stompClient.activate();
    }
  }

  disconnect() {
    if (this.stompClient.active) {
      this.stompClient.deactivate();
    }
  }

  sendMessage(message: ChatMessage) {
    if (this.stompClient.active) {
      this.stompClient.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(message)
      });
    } else {
      // Queue message to be sent once connection is established
      this.pendingMessages.push(message);
    }
  }

  getMessages(): Observable<ChatMessage> {
    return this.messageSubject.asObservable();
  }

  getChatHistory(user1: string, user2: string): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.API_URL}/history/${user1}/${user2}`);
  }

  getRecentContacts(userId: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.API_URL}/contacts/${userId}`);
  }
}
