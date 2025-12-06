import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PortfolioService } from '../../../services/portfolio.service';
import { CreatePortfolioRequest, UpdatePortfolioRequest } from '../../../models/portfolio';

/**
 * Portfolio form component.
 * Handles create and edit operations.
 */
@Component({
  selector: 'app-portfolio-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './portfolio-form.component.html',
  styleUrl: './portfolio-form.component.scss'
})
export class PortfolioFormComponent implements OnInit {
  portfolioForm: FormGroup;
  isEditMode = false;
  portfolioId?: number;
  loading = false;
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private portfolioService: PortfolioService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {
    this.portfolioForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]],
      initialCash: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.portfolioId = +id;
      this.loadPortfolio();
      // Remove initialCash field in edit mode
      this.portfolioForm.removeControl('initialCash');
    }
  }

  private loadPortfolio(): void {
    if (!this.portfolioId) return;

    this.loading = true;
    this.portfolioService.getById(this.portfolioId).subscribe({
      next: (portfolio) => {
        this.portfolioForm.patchValue({
          name: portfolio.name,
          description: portfolio.description
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading portfolio:', error);
        this.snackBar.open('Failed to load portfolio', 'Close', { duration: 3000 });
        this.router.navigate(['/portfolios']);
      }
    });
  }

  onSubmit(): void {
    if (this.portfolioForm.invalid) {
      this.portfolioForm.markAllAsTouched();
      return;
    }

    this.submitting = true;

    if (this.isEditMode && this.portfolioId) {
      const request: UpdatePortfolioRequest = {
        name: this.portfolioForm.value.name,
        description: this.portfolioForm.value.description
      };

      this.portfolioService.update(this.portfolioId, request).subscribe({
        next: () => {
          this.snackBar.open('Portfolio updated successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/portfolios', this.portfolioId]);
        },
        error: (error) => {
          console.error('Error updating portfolio:', error);
          this.snackBar.open(error.error?.message || 'Failed to update portfolio', 'Close', { duration: 3000 });
          this.submitting = false;
        }
      });
    } else {
      const request: CreatePortfolioRequest = {
        name: this.portfolioForm.value.name,
        description: this.portfolioForm.value.description,
        initialCash: this.portfolioForm.value.initialCash
      };

      this.portfolioService.create(request).subscribe({
        next: (portfolio) => {
          this.snackBar.open('Portfolio created successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/portfolios', portfolio.id]);
        },
        error: (error) => {
          console.error('Error creating portfolio:', error);
          this.snackBar.open(error.error?.message || 'Failed to create portfolio', 'Close', { duration: 3000 });
          this.submitting = false;
        }
      });
    }
  }

  getErrorMessage(fieldName: string): string {
    const field = this.portfolioForm.get(fieldName);
    if (!field) return '';

    if (field.hasError('required')) {
      return `${fieldName} is required`;
    }
    if (field.hasError('minlength')) {
      return `${fieldName} must be at least ${field.errors?.['minlength'].requiredLength} characters`;
    }
    if (field.hasError('maxlength')) {
      return `${fieldName} cannot exceed ${field.errors?.['maxlength'].requiredLength} characters`;
    }
    if (field.hasError('min')) {
      return `${fieldName} must be positive`;
    }
    return '';
  }
}