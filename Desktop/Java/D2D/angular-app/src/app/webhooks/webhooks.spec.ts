import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Webhooks } from './webhooks';

describe('Webhooks', () => {
  let component: Webhooks;
  let fixture: ComponentFixture<Webhooks>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Webhooks]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Webhooks);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
