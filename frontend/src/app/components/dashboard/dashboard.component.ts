import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PortfolioService } from '../../services/portfolio.service';
import { TransactionService } from '../../services/transaction.service';
import { Portfolio } from '../../models/portfolio';
import { Transaction } from '../../models/transaction';

/**
 * Dashboard overview component.
 * Displays summary cards and recent activity.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  portfolios: Portfolio[] = [];
  recentTransactions: Transaction[] = [];
  loading = true;

  totalValue = 0;
  totalCash = 0;
  totalPositions = 0;

  constructor(
    private portfolioService: PortfolioService,
    private transactionService: TransactionService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.loading = true;

    this.portfolioService.getAll().subscribe({
      next: (portfolios) => {
        this.portfolios = portfolios;
        this.calculateTotals();
      },
      error: (error) => {
        console.error('Error loading portfolios:', error);
        this.loading = false;
      }
    });

    this.transactionService.getRecentTransactions().subscribe({
      next: (transactions) => {
        this.recentTransactions = transactions;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading transactions:', error);
        this.loading = false;
      }
    });
  }

  private calculateTotals(): void {
    this.totalValue = this.portfolios.reduce((sum, p) => sum + p.totalValue, 0);
    this.totalCash = this.portfolios.reduce((sum, p) => sum + p.availableCash, 0);
    this.totalPositions = this.portfolios.reduce((sum, p) => sum + p.positionCount, 0);
  }

  getTransactionIcon(type: string): string {
    const icons: Record<string, string> = {
      'BUY': 'arrow_upward',
      'SELL': 'arrow_downward',
      'DEPOSIT': 'add_circle',
      'WITHDRAWAL': 'remove_circle',
      'DIVIDEND': 'money'
    };
    return icons[type] || 'swap_horiz';
  }
}