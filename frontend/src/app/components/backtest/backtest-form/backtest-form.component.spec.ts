import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BacktestFormComponent } from './backtest-form.component';

describe('BacktestFormComponent', () => {
  let component: BacktestFormComponent;
  let fixture: ComponentFixture<BacktestFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BacktestFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BacktestFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
