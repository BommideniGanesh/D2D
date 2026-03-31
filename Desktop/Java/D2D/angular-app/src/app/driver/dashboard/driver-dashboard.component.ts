import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { DriverService } from '../../services/driver.service';
import { StorageService } from '../../services/storage.service';
import { DriverDashboardDTO } from '../../models/driver-dashboard.model';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

type Tab = 'pending' | 'assigned' | 'pickedup' | 'delivered' | 'pod' | 'rejected';

@Component({
    selector: 'app-driver-dashboard',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './driver-dashboard.component.html',
    styleUrls: ['./driver-dashboard.component.css']
})
export class DriverDashboardComponent implements OnInit {
    today: number = Date.now();
    loading = true;
    driverId: number | null = null;
    errorMessage: string = '';
    assignmentLoading = false;
    assignmentMessage: string = '';
    lastAssignmentTriggeredAt = 0; // epoch ms, for cooldown

    // Raw data
    allAssignments: DriverDashboardDTO[] = [];
    pendingOrders: any[] = [];

    // Persisted skipped orders (server-side)
    skippedShipmentIds: Set<number> = new Set();
    dismissedPendingOrders: any[] = []; // shown in Rejected tab

    // Tab filtered views
    assigned: DriverDashboardDTO[] = [];
    pickedUp: DriverDashboardDTO[] = [];
    delivered: DriverDashboardDTO[] = [];
    deliveredWithPod: DriverDashboardDTO[] = [];
    rejected: DriverDashboardDTO[] = [];

    activeTab: Tab = 'pending';

    // PoD modal state
    podModalAssignmentId: number | null = null;
    selectedFile: File | null = null;
    imagePreview: string | null = null;
    podSubmitting = false;
    podError: string = '';

    // PoD Details Modal state
    selectedPodOrder: DriverDashboardDTO | null = null;

    openPodDetailsModal(order: DriverDashboardDTO): void {
        this.selectedPodOrder = order;
        const el = document.getElementById('podDetailsModal');
        if (el) el.style.display = 'flex';
    }

    closePodDetailsModal(): void {
        const el = document.getElementById('podDetailsModal');
        if (el) el.style.display = 'none';
        this.selectedPodOrder = null;
    }

    constructor(
        private orderService: OrderService,
        private driverService: DriverService,
        private storageService: StorageService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        const user = this.storageService.getUser();
        if (user && user.id) {
            this.fetchDriverProfile(user.id);
        } else {
            this.errorMessage = 'Please log in to view your dashboard';
            this.loading = false;
        }
    }

    setTab(tab: Tab, skipTrigger = false): void {
        this.activeTab = tab;
        // Auto-trigger batch assignment when viewing pending orders
        // Cooldown: don't trigger if already ran within the last 30s (e.g. on initial page load)
        const cooldownMs = 30_000;
        const now = Date.now();
        if (tab === 'pending' && !this.assignmentLoading && !skipTrigger
            && (now - this.lastAssignmentTriggeredAt) > cooldownMs) {
            this.triggerAssignment();
        }
    }

    fetchDriverProfile(userId: string): void {
        this.driverService.getDriverByUserId(userId).subscribe({
            next: (driver) => {
                if (driver && driver.id) {
                    this.driverId = driver.id;
                    this.loadAll(this.driverId);
                } else {
                    this.errorMessage = 'Driver profile not found. Please complete your profile.';
                    this.loading = false;
                    this.cdr.detectChanges();
                }
            },
            error: (err) => {
                if (err.status === 404) {
                    this.errorMessage = 'Driver profile not found. Please complete your profile.';
                } else {
                    this.errorMessage = 'Could not load driver profile.';
                }
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    loadAll(driverId: number): void {
        forkJoin({
            assignments: this.orderService.getDriverDashboard(driverId).pipe(catchError(() => of([]))),
            pending: this.orderService.getPendingShipments().pipe(catchError(() => of([]))),
            skipped: this.orderService.getSkippedShipments(driverId).pipe(catchError(() => of([])))
        }).subscribe({
            next: ({ assignments, pending, skipped }) => {
                this.allAssignments = assignments;
                this.computeTabs();

                // Build set of skipped IDs for fast filtering
                this.skippedShipmentIds = new Set(skipped.map(s => s.shipmentId as number));

                // Store skipped order details for the Rejected tab
                this.dismissedPendingOrders = skipped;

                // Filter pending orders — exclude already skipped shipments
                this.pendingOrders = pending.filter(
                    (o: any) => !this.skippedShipmentIds.has(o.id)
                );

                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                // Each call has catchError — this only fires for truly catastrophic errors
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    computeTabs(): void {
        // ASSIGNED: only PICKUP assignments not yet acted on
        this.assigned = this.allAssignments.filter(
            a => a.assignmentType === 'PICKUP' && a.assignmentStatus === 'ASSIGNED'
        );

        // PICKED UP (in transit): PICKUP completed, but no completed DELIVERY yet
        const deliveredShipmentIds = new Set(
            this.allAssignments
                .filter(a => a.assignmentType === 'DELIVERY' && a.assignmentStatus === 'COMPLETED')
                .map(a => a.shipmentId)
        );
        this.pickedUp = this.allAssignments.filter(
            a => a.assignmentType === 'PICKUP' && a.assignmentStatus === 'COMPLETED'
                && !deliveredShipmentIds.has(a.shipmentId)
        );

        // DELIVERED: DELIVERY completed but no PoD image yet
        this.delivered = this.allAssignments.filter(
            a => a.assignmentType === 'DELIVERY' && a.assignmentStatus === 'COMPLETED' && !a.podImageUrl
        );

        // DELIVERED + POD: DELIVERY completed with PoD image
        this.deliveredWithPod = this.allAssignments.filter(
            a => a.assignmentType === 'DELIVERY' && a.assignmentStatus === 'COMPLETED' && !!a.podImageUrl
        );

        // REJECTED: any assignment that was rejected or cancelled
        this.rejected = this.allAssignments.filter(
            a => a.assignmentStatus === 'REJECTED' || a.assignmentStatus === 'CANCELLED'
        );
    }

    refresh(): void {
        if (this.driverId) {
            this.loading = true;
            this.loadAll(this.driverId);
        }
    }

    /** Reload data without hiding the page (no loading spinner) */
    silentRefresh(): void {
        if (this.driverId) {
            this.loadAll(this.driverId);
        }
    }

    triggerAssignment(): void {
        this.assignmentLoading = true;
        this.lastAssignmentTriggeredAt = Date.now();
        this.assignmentMessage = '';
        this.orderService.triggerBatchAssignment(10).subscribe({
            next: (response) => {
                this.assignmentLoading = false;
                this.assignmentMessage = `✓ Assigned ${response.assignedCount} shipments`;
                this.silentRefresh(); // don't show loading spinner — just quietly update
                setTimeout(() => { this.assignmentMessage = ''; }, 3000);
            },
            error: () => {
                this.assignmentLoading = false;
                this.assignmentMessage = '✗ Assignment failed';
                setTimeout(() => { this.assignmentMessage = ''; }, 3000);
            }
        });
    }

    completePickup(assignmentId: number): void {
        if (!confirm('Confirm: you have picked up this order?')) return;
        this.orderService.completePickup(assignmentId).subscribe({
            next: () => { this.refresh(); },
            error: (err) => alert('Pickup failed: ' + (err.error?.message || 'Unknown error'))
        });
    }

    // ---- PoD Modal ----
    markAsDelivered(shipmentId: number): void {
        const deliveryAssignment = this.allAssignments.find(
            a => a.shipmentId === shipmentId && a.assignmentType === 'DELIVERY' && a.assignmentStatus === 'ASSIGNED'
        );
        if (deliveryAssignment) {
            if (!confirm('Are you sure you want to mark this as delivered?')) return;
            this.orderService.completeDelivery(deliveryAssignment.assignmentId).subscribe({
                next: () => {
                    this.silentRefresh();
                    this.setTab('delivered', true);
                },
                error: (err) => alert('Failed to mark as delivered: ' + (err.error?.message || 'Try again'))
            });
        } else {
            alert('Could not find delivery assignment for this order.');
        }
    }

    /** Open PoD modal from the PickedUp tab — finds the DELIVERY assignment for this shipment */
    openPodModalForShipment(shipmentId: number): void {
        const delivery = this.allAssignments.find(
            a => a.shipmentId === shipmentId && a.assignmentType === 'DELIVERY' && a.assignmentStatus === 'ASSIGNED'
        );
        if (delivery) {
            this.openPodModal(delivery.assignmentId);
        } else {
            alert('No pending delivery assignment found. Try refreshing the page.');
        }
    }

    openPodModal(assignmentId: number): void {
        this.podModalAssignmentId = assignmentId;
        this.selectedFile = null;
        this.imagePreview = null;
        this.podError = '';
        this.podSubmitting = false;
        const el = document.getElementById('podModal');
        if (el) el.style.display = 'flex';
    }

    closePodModal(): void {
        const el = document.getElementById('podModal');
        if (el) el.style.display = 'none';
        this.podModalAssignmentId = null;
    }

    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files[0]) {
            this.selectedFile = input.files[0];
            const reader = new FileReader();
            reader.onload = (e) => { this.imagePreview = e.target?.result as string; };
            reader.readAsDataURL(this.selectedFile);
        }
    }

    submitPod(): void {
        if (!this.podModalAssignmentId || !this.selectedFile) {
            this.podError = 'Please select an image first.';
            return;
        }
        this.podSubmitting = true;
        this.podError = '';

        this.orderService.uploadPodImage(this.selectedFile).subscribe({
            next: (uploadRes) => {
                this.orderService.submitProofOfDelivery(this.podModalAssignmentId!, uploadRes.imageUrl).subscribe({
                    next: (podRes) => {
                        this.podSubmitting = false;
                        this.closePodModal();
                        alert(`✅ Proof of Delivery submitted!\nML Validation: ${podRes.status}`);
                        this.activeTab = 'pod';
                        this.refresh();
                    },
                    error: (err) => {
                        this.podSubmitting = false;
                        this.podError = err.error?.error || 'PoD submission failed.';
                    }
                });
            },
            error: (err) => {
                this.podSubmitting = false;
                this.podError = err.error?.error || 'Image upload failed.';
            }
        });
    }

    rejectAssignment(assignmentId: number): void {
        if (!confirm('Are you sure you want to REJECT this order?')) return;
        this.orderService.rejectAssignment(assignmentId).subscribe({
            next: () => {
                if (this.driverId) {
                    this.orderService.getDriverDashboard(this.driverId).subscribe({
                        next: (data) => {
                            this.allAssignments = data;
                            this.computeTabs();
                            this.setTab('rejected', true);
                        },
                        error: () => this.setTab('rejected', true)
                    });
                }
            },
            error: (err) => alert('Rejection failed: ' + (err.error?.message || 'Unknown error'))
        });
    }

    pickupPendingOrder(shipmentId: number): void {
        if (!confirm('Are you sure you want to pick up this pending order?')) return;
        if (!this.driverId) return;
        this.orderService.assignPendingShipment(shipmentId, this.driverId).subscribe({
            next: () => { 
                this.silentRefresh();
                this.setTab('pickedup', true);
            },
            error: (err) => alert('Failed to pick up order: ' + (err.error?.message || 'Try again'))
        });
    }

    /** Persist "Not Interested" to DB so it survives page refresh */
    dismissPendingOrder(shipmentId: number): void {
        if (!this.driverId) return;
        const order = this.pendingOrders.find(o => o.id === shipmentId);
        if (!order) return;

        this.orderService.skipShipment(this.driverId, shipmentId).subscribe({
            next: () => {
                // Optimistically update UI
                this.skippedShipmentIds.add(shipmentId);
                this.dismissedPendingOrders = [...this.dismissedPendingOrders, {
                    shipmentId: order.id,
                    trackingNumber: order.trackingNumber,
                    senderName: (order.senderDetails?.firstName ?? '') + ' ' + (order.senderDetails?.lastName ?? ''),
                    senderAddress: (order.senderDetails?.addressLine1 ?? '') + ', ' + (order.senderDetails?.city ?? ''),
                    receiverName: (order.receiverDetails?.firstName ?? '') + ' ' + (order.receiverDetails?.lastName ?? ''),
                    receiverAddress: order.receiverDetails?.address?.addressLine1 ?? ''
                }];
                this.pendingOrders = this.pendingOrders.filter(o => o.id !== shipmentId);
                this.setTab('rejected', true);
            },
            error: () => {
                // Still filter locally even if API fails
                this.pendingOrders = this.pendingOrders.filter(o => o.id !== shipmentId);
            }
        });
    }

    openNavigation(order: DriverDashboardDTO): void {
        const origin = encodeURIComponent(`${order.pickupAddress}, ${order.pickupCity}`);
        const destination = encodeURIComponent(`${order.deliveryAddress}, ${order.deliveryCity}`);
        const mapUrl = `https://www.google.com/maps/dir/?api=1&origin=${origin}&destination=${destination}`;
        window.open(mapUrl, '_blank');
    }

    onImageError(event: Event): void {
        const svgPlaceholder = `data:image/svg+xml;charset=UTF-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="300" viewBox="0 0 400 300"%3E%3Crect width="100%25" height="100%25" fill="%23f1f5f9"/%3E%3Ctext x="50%25" y="50%25" font-family="sans-serif" font-size="16" text-anchor="middle" alignment-baseline="middle" fill="%2394a3b8"%3ENo Image Uploaded%3C/text%3E%3C/svg%3E`;
        (event.target as HTMLImageElement).src = svgPlaceholder;
    }
}
