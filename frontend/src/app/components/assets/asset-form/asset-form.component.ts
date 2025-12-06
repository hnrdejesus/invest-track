import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AssetService } from '../../../services/asset.service';
import { AssetType, CreateAssetRequest, UpdateAssetRequest } from '../../../models/asset';

/**
 * Asset form component.
 * Create and edit assets in the catalog.
 */
@Component({
  selector: 'app-asset-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './asset-form.component.html',
  styleUrl: './asset-form.component.scss'
})
export class AssetFormComponent implements OnInit {
  assetForm: FormGroup;
  isEditMode = false;
  assetId?: number;
  loading = false;
  submitting = false;

  assetTypes = Object.values(AssetType);

  constructor(
    private fb: FormBuilder,
    private assetService: AssetService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {
    this.assetForm = this.fb.group({
      ticker: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(20)]],
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
      assetType: ['', Validators.required],
      currency: ['USD', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
      exchange: ['', Validators.maxLength(50)]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.assetId = +id;
      this.loadAsset();
      // Remove fields that can't be edited
      this.assetForm.get('ticker')?.disable();
      this.assetForm.get('assetType')?.disable();
      this.assetForm.get('currency')?.disable();
    }
  }

  private loadAsset(): void {
    if (!this.assetId) return;

    this.loading = true;
    this.assetService.getById(this.assetId).subscribe({
      next: (asset) => {
        this.assetForm.patchValue({
          ticker: asset.ticker,
          name: asset.name,
          assetType: asset.assetType,
          currency: asset.currency,
          exchange: asset.exchange
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading asset:', error);
        this.snackBar.open('Failed to load asset', 'Close', { duration: 3000 });
        this.router.navigate(['/assets']);
      }
    });
  }

  onSubmit(): void {
    if (this.assetForm.invalid) {
      this.assetForm.markAllAsTouched();
      return;
    }

    this.submitting = true;

    if (this.isEditMode && this.assetId) {
      const request: UpdateAssetRequest = {
        name: this.assetForm.value.name,
        exchange: this.assetForm.value.exchange
      };

      this.assetService.update(this.assetId, request).subscribe({
        next: () => {
          this.snackBar.open('Asset updated successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/assets']);
        },
        error: (error) => {
          console.error('Error updating asset:', error);
          this.snackBar.open(error.error?.message || 'Failed to update asset', 'Close', { duration: 3000 });
          this.submitting = false;
        }
      });
    } else {
      const request: CreateAssetRequest = {
        ticker: this.assetForm.value.ticker.toUpperCase(),
        name: this.assetForm.value.name,
        assetType: this.assetForm.value.assetType,
        currency: this.assetForm.value.currency.toUpperCase(),
        exchange: this.assetForm.value.exchange
      };

      this.assetService.create(request).subscribe({
        next: () => {
          this.snackBar.open('Asset created successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/assets']);
        },
        error: (error) => {
          console.error('Error creating asset:', error);
          this.snackBar.open(error.error?.message || 'Failed to create asset', 'Close', { duration: 3000 });
          this.submitting = false;
        }
      });
    }
  }
}