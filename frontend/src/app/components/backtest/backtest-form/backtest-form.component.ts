import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { PortfolioService } from '../../../services/portfolio.service';
import { BacktestService } from '../../../services/backtest.service';
import { Portfolio } from '../../../models/portfolio';
import { BacktestStrategy } from '../../../models/backtest-strategy';
import { BacktestResult } from '../../../models/backtest-result';

Chart.register(...registerables);

/**
 * Backtest form component.
 * Test trading strategies on historical data.
 */
@Component({
  selector: 'app-backtest-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './backtest-form.component.html',
  styleUrl: './backtest-form.component.scss'
})
export class BacktestFormComponent implements OnInit {
  @ViewChild('chartCanvas', { static: false }) chartCanvas!: ElementRef<HTMLCanvasElement>;
  
  backtestForm: FormGroup;
  portfolios: Portfolio[] = [];
  result?: BacktestResult;
  loading = false;
  running = false;
  chart?: Chart;

  constructor(
    private fb: FormBuilder,
    private portfolioService: PortfolioService,
    private backtestService: BacktestService,
    private snackBar: MatSnackBar
  ) {
    this.backtestForm = this.fb.group({
      portfolioId: ['', Validators.required],
      strategyName: ['', [Validators.required, Validators.minLength(3)]],
      initialCapital: [10000, [Validators.required, Validators.min(1)]],
      buyThreshold: [-0.05, [Validators.required, Validators.min(-1), Validators.max(0)]],
      sellThreshold: [0.10, [Validators.required, Validators.min(0), Validators.max(10)]],
      maxPositionSize: [0.30, [Validators.required, Validators.min(0.01), Validators.max(1)]],
      stopLoss: [-0.15, [Validators.min(-1), Validators.max(0)]],
      takeProfit: [0.25, [Validators.min(0), Validators.max(10)]],
      days: [252, [Validators.required, Validators.min(30), Validators.max(1260)]]
    });
  }

  ngOnInit(): void {
    this.loadPortfolios();
  }

  private loadPortfolios(): void {
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

  runBacktest(): void {
    if (this.backtestForm.invalid) {
      this.backtestForm.markAllAsTouched();
      return;
    }

    this.running = true;
    this.result = undefined;

    const { portfolioId, days, ...strategyData } = this.backtestForm.value;
    const strategy: BacktestStrategy = strategyData;

    this.backtestService.runBacktest(portfolioId, strategy, days).subscribe({
      next: (result) => {
        this.result = result;
        this.running = false;
        this.snackBar.open('Backtest completed', 'Close', { duration: 3000 });
        setTimeout(() => this.createChart(), 100);
      },
      error: (error) => {
        console.error('Error running backtest:', error);
        this.snackBar.open(error.error?.message || 'Backtest failed', 'Close', { duration: 3000 });
        this.running = false;
      }
    });
  }

  private createChart(): void {
    if (!this.chartCanvas || !this.result) return;

    if (this.chart) {
      this.chart.destroy();
    }

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    const labels = this.result.portfolioHistory.map(h => 
      new Date(h.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
    );
    const data = this.result.portfolioHistory.map(h => h.value);

    const config: ChartConfiguration = {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Portfolio Value',
          data,
          borderColor: 'rgba(63, 81, 181, 1)',
          backgroundColor: 'rgba(63, 81, 181, 0.1)',
          borderWidth: 2,
          fill: true,
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            text: 'Portfolio Value Over Time',
            font: { size: 16 }
          },
          legend: {
            display: false
          },
          tooltip: {
            callbacks: {
              label: (context: any) => `Value: ${context.parsed.y.toLocaleString()}`
            }
          }
        },
        scales: {
          x: {
            title: {
              display: true,
              text: 'Date'
            },
            ticks: {
              maxRotation: 45,
              minRotation: 45
            }
          },
          y: {
            title: {
              display: true,
              text: 'Portfolio Value ($)'
            },
            beginAtZero: false
          }
        }
      }
    };

    this.chart = new Chart(ctx, config);
  }

  formatPercentage(value: number): string {
    const sign = value >= 0 ? '+' : '';
    return `${sign}${(value * 100).toFixed(2)}%`;
  }

  getReturnClass(value: number): string {
    return value >= 0 ? 'profit' : 'loss';
  }
}