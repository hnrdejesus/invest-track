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
import { MonteCarloService } from '../../../services/monte-carlo.service';
import { Portfolio } from '../../../models/portfolio';
import { MonteCarloSimulation } from '../../../models/monte-carlo-simulation';

Chart.register(...registerables);

/**
 * Monte Carlo simulation component.
 * Probabilistic portfolio projections with distribution chart.
 */
@Component({
  selector: 'app-monte-carlo-simulation',
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
  templateUrl: './monte-carlo-simulation.component.html',
  styleUrl: './monte-carlo-simulation.component.scss'
})
export class MonteCarloSimulationComponent implements OnInit {
  @ViewChild('chartCanvas', { static: false }) chartCanvas!: ElementRef<HTMLCanvasElement>;
  
  simulationForm: FormGroup;
  portfolios: Portfolio[] = [];
  result?: MonteCarloSimulation;
  loading = false;
  running = false;
  chart?: Chart;

  constructor(
    private fb: FormBuilder,
    private portfolioService: PortfolioService,
    private monteCarloService: MonteCarloService,
    private snackBar: MatSnackBar
  ) {
    this.simulationForm = this.fb.group({
      portfolioId: ['', Validators.required],
      iterations: [10000, [Validators.required, Validators.min(1000), Validators.max(100000)]],
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

  runSimulation(): void {
    if (this.simulationForm.invalid) {
      this.simulationForm.markAllAsTouched();
      return;
    }

    this.running = true;
    this.result = undefined;

    const { portfolioId, iterations, days } = this.simulationForm.value;

    this.monteCarloService.runSimulation(portfolioId, iterations, days).subscribe({
      next: (result) => {
        this.result = result;
        this.running = false;
        this.snackBar.open('Simulation completed', 'Close', { duration: 3000 });
        setTimeout(() => this.createChart(), 100);
      },
      error: (error) => {
        console.error('Error running simulation:', error);
        this.snackBar.open(error.error?.message || 'Simulation failed', 'Close', { duration: 3000 });
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

    // Create histogram data
    const sortedResults = [...this.result.simulationResults].sort((a, b) => a - b);
    const bins = 50;
    const min = sortedResults[0];
    const max = sortedResults[sortedResults.length - 1];
    const binSize = (max - min) / bins;

    const histogram = new Array(bins).fill(0);
    sortedResults.forEach(value => {
      const binIndex = Math.min(Math.floor((value - min) / binSize), bins - 1);
      histogram[binIndex]++;
    });

    const labels = Array.from({ length: bins }, (_, i) => {
      const binStart = min + i * binSize;
      return `$${(binStart / 1000).toFixed(0)}k`;
    });

    const config: ChartConfiguration = {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: 'Frequency',
          data: histogram,
          backgroundColor: 'rgba(63, 81, 181, 0.7)',
          borderColor: 'rgba(63, 81, 181, 1)',
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            text: 'Distribution of Simulation Results',
            font: { size: 16 }
          },
          legend: {
            display: false
          },
          tooltip: {
            callbacks: {
              label: (context) => `Frequency: ${context.parsed.y}`
            }
          }
        },
        scales: {
          x: {
            title: {
              display: true,
              text: 'Portfolio Value'
            },
            ticks: {
              maxRotation: 45,
              minRotation: 45
            }
          },
          y: {
            title: {
              display: true,
              text: 'Frequency'
            },
            beginAtZero: true
          }
        }
      }
    };

    this.chart = new Chart(ctx, config);
  }

  formatPercentage(value: number): string {
    return `${(value * 100).toFixed(2)}%`;
  }
}