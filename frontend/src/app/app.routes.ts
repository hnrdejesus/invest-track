import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { PortfolioListComponent } from './components/portfolios/portfolio-list/portfolio-list.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'portfolios', component: PortfolioListComponent },
  // More routes will be added as more components are built.
];