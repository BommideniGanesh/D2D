import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Tracking } from '../tracking/tracking';

@Component({
    selector: 'app-home',
    standalone: true,
    imports: [CommonModule, Tracking],
    templateUrl: './home.component.html',
    styleUrl: './home.component.css'
})
export class HomeComponent {
    images = [
        'assets/carousel/image1.jpg',
        'assets/carousel/image2.jpg',
        'assets/carousel/image3.jpg',
        'assets/carousel/image4.jpg',
        'assets/carousel/image5.jpg'
    ];
}
