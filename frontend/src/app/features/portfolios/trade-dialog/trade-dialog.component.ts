import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PositionService } from '../../../services/position.service';
import { AssetService } from '../../../services/asset.service';
import { Asset } from '../../../models/asset';
import { TradeRequest } from '../../../models/position';

@Component({
  selector: 'app-trade-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './trade-dialog.component.html',
  styleUrls: ['./trade-dialog.component.scss']
})
export class TradeDialogComponent implements OnInit {
  tradeForm: FormGroup;
  assets: Asset[] = [];
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<TradeDialogComponent>,
    private positionService: PositionService,
    private assetService: AssetService,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: {
      portfolioId: number;
      type: 'buy' | 'sell';
      availableCash?: number;
    }
  ) {
    this.tradeForm = this.fb.group({
      assetId: [null, Validators.required],
      quantity: [null, [Validators.required, Validators.min(0.00000001)]],
      price: [null, [Validators.required, Validators.min(0.01)]],
      fees: [0, Validators.min(0)]
    });
  }

  ngOnInit(): void {
    this.assetService.getAll().subscribe({
      next: (assets) => this.assets = assets.filter(a => a.active),
      error: () => this.snackBar.open('Failed to load assets', 'Close', { duration: 3000 })
    });
  }

  get isBuying(): boolean {
    return this.data.type === 'buy';
  }

  get totalCost(): number {
    const { quantity, price, fees } = this.tradeForm.value;
    return (quantity * price) + (fees || 0);
  }

  get showTotal(): boolean {
    const { quantity, price } = this.tradeForm.value;
    return !!(quantity && price);
  }

  onSubmit(): void {
    if (this.tradeForm.invalid) return;

    this.submitting = true;
    const request: TradeRequest = { ...this.tradeForm.value, fees: this.tradeForm.value.fees || 0 };
    const operation = this.isBuying
      ? this.positionService.buy(this.data.portfolioId, request)
      : this.positionService.sell(this.data.portfolioId, request);

    operation.subscribe({
      next: (position) => {
        this.snackBar.open(
          `${this.isBuying ? 'Purchase' : 'Sale'} completed successfully`,
          'Close',
          { duration: 3000 }
        );
        this.dialogRef.close(position);
      },
      error: (error) => {
        this.snackBar.open(
          error.error?.message || `Failed to ${this.data.type} asset`,
          'Close',
          { duration: 5000 }
        );
        this.submitting = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}