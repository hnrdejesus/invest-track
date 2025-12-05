import { TestBed } from '@angular/core/testing';

import { BacktestService } from './backtest.service';

describe('BacktestService', () => {
  let service: BacktestService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BacktestService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
