import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { HomeComponent } from './home/home.component';
import { CreateOrder } from './create-order/create-order';
import { MyOrders } from './my-orders/my-orders';
import { Tracking } from './tracking/tracking';
import { DeliveredOrdersComponent } from './delivered-orders/delivered-orders';
import { EarningsComponent } from './earnings/earnings';
import { PendingOrdersComponent } from './pending-orders/pending-orders';
import { DetailsComponent } from './details/details';
import { DriverDashboardComponent } from './driver/dashboard/driver-dashboard.component';

import { AuthGuard } from './guards/auth.guard';

import { RoleGuard } from './guards/role.guard';
import { AdminDashboardComponent } from './admin/dashboard/admin-dashboard.component';
import { HireDriverComponent } from './admin/hire-driver/hire-driver.component';

import { ForgotPasswordComponent } from './forgot-password/forgot-password';
import { ProfileComponent } from './profile/profile.component';
import { WalletComponent } from './wallet/wallet.component';
import { WebhooksComponent } from './webhooks/webhooks.component';
import { WarehouseComponent } from './warehouse/warehouse.component';
import { LiveTrackingComponent } from './live-tracking/live-tracking.component';
import { SupportChatComponent } from './support-chat/support-chat.component';
import { OperationChatComponent } from './operation-chat/operation-chat.component';

export const routes: Routes = [
    { path: 'admin/dashboard', component: AdminDashboardComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'admin/hire-driver', component: HireDriverComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'driver-dashboard', component: DriverDashboardComponent },
    { path: '', component: HomeComponent },
    { path: 'login', component: LoginComponent },
    { path: 'forgot-password', component: ForgotPasswordComponent },
    { path: 'profile', component: ProfileComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'create-order', component: CreateOrder, canActivate: [AuthGuard] },
    { path: 'my-orders', component: MyOrders },
    { path: 'tracking', component: Tracking },
    { path: 'delivered-orders', component: DeliveredOrdersComponent },
    { path: 'earnings', component: EarningsComponent },
    { path: 'pending-orders', component: PendingOrdersComponent },
    { path: 'details', component: DetailsComponent },
    { path: 'wallet', component: WalletComponent, canActivate: [AuthGuard] },
    { path: 'webhooks', component: WebhooksComponent, canActivate: [AuthGuard] },
    { path: 'warehouse', component: WarehouseComponent, canActivate: [AuthGuard, RoleGuard], data: { roles: ['ADMIN'] } },
    { path: 'live-tracking/:shipmentId', component: LiveTrackingComponent },
    { path: 'support-chat', component: SupportChatComponent, canActivate: [AuthGuard] },
    { path: 'operation-chat', component: OperationChatComponent, canActivate: [AuthGuard] },
];
