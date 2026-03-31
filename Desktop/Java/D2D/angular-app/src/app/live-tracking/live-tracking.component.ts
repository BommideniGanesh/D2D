import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Component({
  selector: 'app-live-tracking',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './live-tracking.component.html',
  styleUrls: ['./live-tracking.component.css']
})
export class LiveTrackingComponent implements OnInit, OnDestroy {
  shipmentId: string | null = null;
  private stompClient: Client | null = null;
  
  // Real-time coordinates
  latitude: number = 40.7128; // Default NYC
  longitude: number = -74.0060;
  pingedAt: string | null = null;
  
  connectionStatus: string = 'DISCONNECTED';
  connectionError: string = '';

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.shipmentId = this.route.snapshot.paramMap.get('shipmentId');
    if (this.shipmentId) {
      this.connectWebSocket();
    }
  }

  ngOnDestroy() {
    this.disconnectWebSocket();
  }

  private connectWebSocket() {
    this.connectionStatus = 'CONNECTING...';
    
    // Explicitly targeting the Java WebSockets native router mapping
    const socket = new SockJS('http://localhost:8080/ws');
    
    this.stompClient = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      this.connectionStatus = 'CONNECTED (LIVE)';
      this.connectionError = '';
      
      // Actively intercept incoming payloads over individual shipment topologies
      this.stompClient?.subscribe(`/topic/tracking/${this.shipmentId}`, (message) => {
        if (message.body) {
          const payload = JSON.parse(message.body);
          this.latitude = payload.latitude;
          this.longitude = payload.longitude;
          this.pingedAt = payload.pingedAt;
        }
      });
    };

    this.stompClient.onStompError = (frame) => {
      this.connectionStatus = 'ERROR';
      this.connectionError = 'Broker reported error: ' + frame.headers['message'];
      console.error(this.connectionError);
    };

    this.stompClient.activate();
  }

  private disconnectWebSocket() {
    if (this.stompClient !== null) {
      this.stompClient.deactivate();
    }
    this.connectionStatus = 'DISCONNECTED';
  }
}
