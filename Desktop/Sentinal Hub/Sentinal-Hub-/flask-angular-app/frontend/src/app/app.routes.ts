import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { MlTrading } from './pages/ml-trading/ml-trading';
import { MarketAnalysis } from './pages/market-analysis/market-analysis';
import { SentimentFeed } from './pages/sentiment-feed/sentiment-feed';
import { TradeHistory } from './pages/trade-history/trade-history';
import { Settings } from './pages/settings/settings';
import { KycDetails } from './pages/kyc-details/kyc-details';
import { KycUpload } from './pages/kyc-upload/kyc-upload';
import { KycPending } from './pages/kyc-pending/kyc-pending';
import { AdminKyc } from './pages/admin-kyc/admin-kyc';
import { kycGuard } from './guards/kyc.guard';

export const routes: Routes = [
    { path: 'login', component: Login },
    { path: 'register', component: Register },
    { path: 'kyc-details', component: KycDetails },
    { path: 'kyc-upload', component: KycUpload },
    { path: 'kyc-pending', component: KycPending },
    { path: 'admin-kyc', component: AdminKyc },
    { path: 'ml-trading', component: MlTrading, canActivate: [kycGuard] },
    { path: 'market-analysis', component: MarketAnalysis, canActivate: [kycGuard] },
    { path: 'sentiment-feed', component: SentimentFeed, canActivate: [kycGuard] },
    { path: 'trade-history', component: TradeHistory, canActivate: [kycGuard] },
    { path: 'settings', component: Settings, canActivate: [kycGuard] },
    { path: '', redirectTo: '/market-analysis', pathMatch: 'full' }
];
