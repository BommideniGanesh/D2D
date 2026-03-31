import { Component } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { StorageService } from '../services/storage.service';
import { LoaderService } from '../services/loader.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  activeLink: string = 'Home';
  showProfileMenu = false;

  constructor(
    private storageService: StorageService,
    private router: Router,
    private loaderService: LoaderService
  ) { }

  get isLoggedIn(): boolean {
    return this.storageService.isLoggedIn();
  }

  getUserRoles(): string[] {
    const user = this.storageService.getUser();
    return user?.roles || [];
  }

  isDriver(): boolean {
    const roles = this.getUserRoles();
    return roles.includes('DRIVER');
  }

  isAdmin(): boolean {
    const roles = this.getUserRoles();
    return roles.includes('ADMIN');
  }

  setActive(link: string) {
    this.activeLink = link;
  }

  toggleProfileMenu() {
    this.showProfileMenu = !this.showProfileMenu;
  }

  logout() {
    this.loaderService.show();
    this.storageService.clean();
    this.router.navigate(['/login']);
  }
}
