import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { PortfolioService } from '../../../services/portfolio.service';
import { PositionService } from '../../../services/position.service';
import { MetricsService } from '../../../services/metrics.service';
import { Portfolio } from '../../../models/portfolio';
import { Position } from '../../../models/position';
import { PortfolioMetrics } from '../../../models/portfolio-metrics';

/**
 * Portfolio detail component.
 * Shows portfolio overview, positions, and metrics.
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
    MatTabsModule
  ],
  templateUrl: './portfolio-detail.component.html',
  styleUrl: './portfolio-detail.component.scss'
})
export class PortfolioDetailComponent implements OnInit {
  portfolio?: Portfolio;
  positions: Position[] = [];
  metrics?: PortfolioMetrics;
  loading = true;

  displayedColumns: string[] = ['asset', 'quantity', 'avgPrice', 'currentValue', 'profitLoss'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private portfolioService: PortfolioService,
    private positionService: PositionService,
    private metricsService: MetricsService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadPortfolioData(+id);
    }
  }

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

  getProfitLossClass(profitLoss: number): string {
    return profitLoss >= 0 ? 'profit' : 'loss';
  }

  formatPercentage(value: number): string {
    const sign = value >= 0 ? '+' : '';
    return `${sign}${(value * 100).toFixed(2)}%`;
  }
}