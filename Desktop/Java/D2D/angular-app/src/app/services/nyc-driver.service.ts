import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { NycDriver } from '../models/nyc-driver.model';

@Injectable({
  providedIn: 'root'
})
export class NycDriverService {
  private readonly API_URL = '/nyc-api/api/odata/v4/8wbx-tsch';
  private readonly APP_TOKEN = '589r9bjzv4128xo27kp8etyo4';
  private readonly PAGE_SIZE = 25;

  constructor(private http: HttpClient) { }

  getDrivers(page: number = 0, search: string = ''): Observable<NycDriver[]> {
    const headers = new HttpHeaders({
      'X-App-Token': this.APP_TOKEN
    });

    let params = new HttpParams()
      .set('$top', this.PAGE_SIZE.toString())
      .set('$skip', (page * this.PAGE_SIZE).toString())
      .set('$orderby', 'name asc');

    if (search && search.trim()) {
      params = params.set('$filter', `contains(tolower(name), tolower('${search.trim()}'))`);
    }

    return this.http.get<any>(this.API_URL, { headers, params }).pipe(
      map(response => response?.value ?? [])
    );
  }
}
