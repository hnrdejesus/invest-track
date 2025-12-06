import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Position list component.
 * This component is not used as standalone - positions are displayed
 * within the portfolio detail component.
 */
@Component({
  selector: 'app-position-list',
  standalone: true,
  imports: [CommonModule],
  template: '<p>This component is integrated into portfolio-detail</p>',
  styles: ['p { padding: 24px; }']
})
export class PositionListComponent {
  // This component is just a placeholder
  // Positions are managed within PortfolioDetailComponent
}