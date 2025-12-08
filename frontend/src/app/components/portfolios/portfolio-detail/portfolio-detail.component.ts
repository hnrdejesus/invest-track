import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { PortfolioService } from '../../../services/portfolio.service';
import { PositionService } from '../../../services/position.service';
import { MetricsService } from '../../../services/metrics.service';
import { Portfolio } from '../../../models/portfolio';
import { Position } from '../../../models/position';
import { PortfolioMetrics } from '../../../models/portfolio-metrics';
import { TradeDialogComponent } from '../../../features/portfolios/trade-dialog/trade-dialog.component';

/**
 * Portfolio detail component.
 * Shows portfolio overview, positions, and metrics.
 * Includes trading functionality via dialog.
 */
@Component({
  selector: 'app-portfolio-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatDialogModule
  ],
  templateUrl: './portfolio-detail.component.html',
  styleUrl: './portfolio-detail.component.scss'
})
export class PortfolioDetailComponent implements OnInit {
  portfolio?: Portfolio;
  positions: Position[] = [];
  metrics?: PortfolioMetrics;
  loading = true;

  displayedColumns: string[] = ['asset', 'quantity', 'avgPrice', 'currentValue', 'profitLoss', 'actions'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private portfolioService: PortfolioService,
    private positionService: PositionService,
    private metricsService: MetricsService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadPortfolioData(+id);
    }
  }

  /**
   * Loads portfolio data including positions and metrics.
   * Makes parallel requests to optimize loading time.
   */
  private loadPortfolioData(id: number): void {
    this.loading = true;

    this.portfolioService.getById(id).subscribe({
      next: (portfolio) => {
        this.portfolio = portfolio;
      },
      error: (error) => {
        console.error('Error loading portfolio:', error);
        this.router.navigate(['/portfolios']);
      }
    });

    this.positionService.getPortfolioPositions(id).subscribe({
      next: (positions) => {
        this.positions = positions;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading positions:', error);
        this.loading = false;
      }
    });

    this.metricsService.getMetrics(id).subscribe({
      next: (metrics) => {
        this.metrics = metrics;
      },
      error: (error) => {
        console.error('Error loading metrics:', error);
      }
    });
  }

  /**
   * Opens buy dialog.
   * Passes available cash to validate purchases.
   */
  openBuyDialog(): void {
    if (!this.portfolio?.id) return;

    const dialogRef = this.dialog.open(TradeDialogComponent, {
      width: '500px',
      data: {
        portfolioId: this.portfolio.id,
        type: 'buy',
        availableCash: this.portfolio.availableCash
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.portfolio?.id) {
        this.loadPortfolioData(this.portfolio.id);
      }
    });
  }

  /**
   * Opens sell dialog for specific position.
   */
  openSellDialog(position: Position): void {
    if (!this.portfolio?.id) return;

    const dialogRef = this.dialog.open(TradeDialogComponent, {
      width: '500px',
      data: {
        portfolioId: this.portfolio.id,
        type: 'sell',
        position: position
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.portfolio?.id) {
        this.loadPortfolioData(this.portfolio.id);
      }
    });
  }

  /**
   * Returns CSS class based on profit/loss value.
   */
  getProfitLossClass(profitLoss: number): string {
    return profitLoss >= 0 ? 'profit' : 'loss';
  }

  /**
   * Formats percentage with sign.
   */
  formatPercentage(value: number): string {
    const sign = value >= 0 ? '+' : '';
    return `${sign}${(value * 100).toFixed(2)}%`;
  }
}