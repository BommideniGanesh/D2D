import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class LoaderService {
    private isLoading = new BehaviorSubject<boolean>(false);
    isLoading$ = this.isLoading.asObservable();

    constructor() { }

    show() {
        // Defer update to avoid NG0100 error
        setTimeout(() => {
            this.isLoading.next(true);
        });
    }

    hide() {
        setTimeout(() => {
            this.isLoading.next(false);
        });
    }
}
