import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class KycService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:5000/api';

  private getHeaders() {
    const token = localStorage.getItem('access_token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  submitDetails(data: any) {
    return this.http.post<any>(`${this.apiUrl}/kyc/details`, data, { headers: this.getHeaders() });
  }

  uploadDocument(file: File) {
    const formData = new FormData();
    formData.append('document', file);
    return this.http.post<any>(`${this.apiUrl}/kyc/upload`, formData, { headers: this.getHeaders() });
  }

  getStatus() {
    return this.http.get<any>(`${this.apiUrl}/kyc/status`, { headers: this.getHeaders() });
  }

  getAllKyc() {
    return this.http.get<any[]>(`${this.apiUrl}/admin/kyc`, { headers: this.getHeaders() });
  }

  approveKyc(id: number) {
    return this.http.post<any>(`${this.apiUrl}/admin/kyc/${id}/approve`, {}, { headers: this.getHeaders() });
  }

  getDocumentUrl(id: number) {
    return `${this.apiUrl}/admin/kyc/document/${id}`;
  }

  getDocumentBlob(id: number) {
    return this.http.get(`${this.apiUrl}/admin/kyc/document/${id}`, { 
      headers: this.getHeaders(),
      responseType: 'blob' 
    });
  }
}
