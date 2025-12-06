import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { AssetService } from '../../../services/asset.service';
import { Asset, AssetType } from '../../../models/asset';

/**
 * Asset list component.
 * Browse and search asset catalog.
 */
@Component({
  selector: 'app-asset-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  templateUrl: './asset-list.component.html',
  styleUrl: './asset-list.component.scss'
})
export class AssetListComponent implements OnInit {
  assets: Asset[] = [];
  displayedColumns: string[] = ['ticker', 'name', 'type', 'price', 'status', 'actions'];
  loading = true;
  searchControl = new FormControl('');

  assetTypes = Object.values(AssetType);

  constructor(
    private assetService: AssetService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadAssets();
    this.setupSearch();
  }

  private setupSearch(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(query => {
        if (query && query.trim().length > 0) {
          this.searchAssets(query.trim());
        } else {
          this.loadAssets();
        }
      });
  }

  loadAssets(): void {
    this.loading = true;
    this.assetService.getAll().subscribe({
      next: (assets) => {
        this.assets = assets;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading assets:', error);
        this.snackBar.open('Failed to load assets', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  private searchAssets(query: string): void {
    this.loading = true;
    this.assetService.search(query).subscribe({
      next: (assets) => {
        this.assets = assets;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error searching assets:', error);
        this.snackBar.open('Search failed', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  filterByType(type: AssetType): void {
    this.loading = true;
    this.assetService.getByType(type).subscribe({
      next: (assets) => {
        this.assets = assets;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error filtering assets:', error);
        this.snackBar.open('Filter failed', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  toggleStatus(asset: Asset): void {
    if (!asset.id) return;

    const action = asset.active 
      ? this.assetService.deactivate(asset.id)
      : this.assetService.reactivate(asset.id);

    action.subscribe({
      next: () => {
        asset.active = !asset.active;
        this.snackBar.open(
          `Asset ${asset.active ? 'activated' : 'deactivated'} successfully`,
          'Close',
          { duration: 3000 }
        );
      },
      error: (error) => {
        console.error('Error updating asset status:', error);
        this.snackBar.open('Failed to update status', 'Close', { duration: 3000 });
      }
    });
  }

  getStatusColor(active: boolean): string {
    return active ? 'primary' : 'warn';
  }
}