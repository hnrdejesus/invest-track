import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { PortfolioListComponent } from './components/portfolios/portfolio-list/portfolio-list.component';
import { PortfolioFormComponent } from './components/portfolios/portfolio-form/portfolio-form.component';
import { PortfolioDetailComponent } from './components/portfolios/portfolio-detail/portfolio-detail.component';
import { AssetListComponent } from './components/assets/asset-list/asset-list.component';
import { AssetFormComponent } from './components/assets/asset-form/asset-form.component';
import { MonteCarloSimulationComponent } from './components/monte-carlo/simulation/monte-carlo-simulation.component';
import { BacktestFormComponent } from './components/backtest/backtest-form/backtest-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'portfolios', component: PortfolioListComponent },
  { path: 'portfolios/new', component: PortfolioFormComponent },
  { path: 'portfolios/:id', component: PortfolioDetailComponent },
  { path: 'portfolios/:id/edit', component: PortfolioFormComponent },
  { path: 'assets', component: AssetListComponent },
  { path: 'assets/new', component: AssetFormComponent },
  { path: 'assets/:id/edit', component: AssetFormComponent },
  { path: 'monte-carlo', component: MonteCarloSimulationComponent },
  { path: 'backtest', component: BacktestFormComponent }
];