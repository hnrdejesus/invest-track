import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { PortfolioService } from '../../../services/portfolio.service';
import { Portfolio } from '../../../models/portfolio';

/**
 * Portfolio list component.
 * Displays all portfolios with CRUD actions.
 */
@Component({
  selector: 'app-portfolio-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './portfolio-list.component.html',
  styleUrl: './portfolio-list.component.scss'
})
export class PortfolioListComponent implements OnInit {
  portfolios: Portfolio[] = [];
  displayedColumns: string[] = ['name', 'totalValue', 'availableCash', 'positionCount', 'actions'];
  loading = true;

  constructor(
    private portfolioService: PortfolioService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadPortfolios();
  }

  loadPortfolios(): void {
    this.loading = true;
    this.portfolioService.getAll().subscribe({
      next: (portfolios) => {
        this.portfolios = portfolios;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading portfolios:', error);
        this.snackBar.open('Failed to load portfolios', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  deletePortfolio(portfolio: Portfolio): void {
    if (!portfolio.id) return;
    
    if (confirm(`Delete portfolio "${portfolio.name}"? This action cannot be undone.`)) {
      this.portfolioService.delete(portfolio.id).subscribe({
        next: () => {
          this.snackBar.open('Portfolio deleted successfully', 'Close', { duration: 3000 });
          this.loadPortfolios();
        },
        error: (error) => {
          console.error('Error deleting portfolio:', error);
          this.snackBar.open('Failed to delete portfolio', 'Close', { duration: 3000 });
        }
      });
    }
  }
}