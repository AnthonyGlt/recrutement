import { SimulationStep } from './types.js';

/**
 * Small chart drawn under the grid: how many cells are burning at each step, and how much of the
 * forest has already burnt.
 */
export class HistoryChart {
  private readonly context: CanvasRenderingContext2D;
  private steps: SimulationStep[] = [];
  private cellCount = 1;

  constructor(private readonly canvas: HTMLCanvasElement) {
    const context = canvas.getContext('2d');
    if (!context) {
      throw new Error('Canvas 2D context is not available');
    }
    this.context = context;
  }

  setData(steps: SimulationStep[], cellCount: number): void {
    this.steps = steps;
    this.cellCount = Math.max(1, cellCount);
  }

  clear(): void {
    this.steps = [];
    this.draw(0);
  }

  draw(currentStep: number): void {
    const ratio = window.devicePixelRatio || 1;
    const width = this.canvas.clientWidth;
    const height = this.canvas.clientHeight;
    this.canvas.width = Math.round(width * ratio);
    this.canvas.height = Math.round(height * ratio);
    this.context.setTransform(ratio, 0, 0, ratio, 0, 0);
    this.context.clearRect(0, 0, width, height);

    if (this.steps.length < 2) {
      return;
    }

    const maxBurning = Math.max(...this.steps.map((step) => step.burning), 1);
    const x = (index: number) => (index / (this.steps.length - 1)) * width;

    // Burnt surface, as a filled area.
    this.context.beginPath();
    this.context.moveTo(0, height);
    this.steps.forEach((step, index) => {
      const burnt = (step.ash + step.burning) / this.cellCount;
      this.context.lineTo(x(index), height - burnt * height);
    });
    this.context.lineTo(width, height);
    this.context.closePath();
    this.context.fillStyle = 'rgba(120, 113, 108, 0.35)';
    this.context.fill();

    // Cells on fire, as a line.
    this.context.beginPath();
    this.steps.forEach((step, index) => {
      const y = height - (step.burning / maxBurning) * (height - 4) - 2;
      if (index === 0) {
        this.context.moveTo(x(index), y);
      } else {
        this.context.lineTo(x(index), y);
      }
    });
    this.context.strokeStyle = '#ff6a1f';
    this.context.lineWidth = 2;
    this.context.stroke();

    // Cursor on the current step.
    const cursor = x(currentStep);
    this.context.beginPath();
    this.context.moveTo(cursor, 0);
    this.context.lineTo(cursor, height);
    this.context.strokeStyle = 'rgba(255, 255, 255, 0.55)';
    this.context.lineWidth = 1;
    this.context.stroke();
  }
}
