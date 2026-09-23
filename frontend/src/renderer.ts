import { Cell, CellState } from './types.js';

/** Draws the forest on a canvas and converts mouse events back into cell coordinates. */
export class ForestRenderer {
  private readonly context: CanvasRenderingContext2D;
  private height = 1;
  private width = 1;
  private cellSize = 1;
  private offsetX = 0;
  private offsetY = 0;

  constructor(private readonly canvas: HTMLCanvasElement) {
    const context = canvas.getContext('2d');
    if (!context) {
      throw new Error('Canvas 2D context is not available');
    }
    this.context = context;
  }

  setGridSize(height: number, width: number): void {
    this.height = height;
    this.width = width;
    this.layout();
  }

  /** Recomputes the size of a cell so that the whole grid fits in the available space. */
  layout(): void {
    const ratio = window.devicePixelRatio || 1;
    const available = this.canvas.parentElement;
    const availableWidth = available ? available.clientWidth : this.canvas.clientWidth;
    const availableHeight = available ? available.clientHeight : this.canvas.clientHeight;

    this.cellSize = Math.max(
      1,
      Math.floor(Math.min(availableWidth / this.width, availableHeight / this.height)),
    );
    const pixelWidth = this.cellSize * this.width;
    const pixelHeight = this.cellSize * this.height;

    this.canvas.style.width = `${pixelWidth}px`;
    this.canvas.style.height = `${pixelHeight}px`;
    this.canvas.width = Math.round(pixelWidth * ratio);
    this.canvas.height = Math.round(pixelHeight * ratio);
    this.context.setTransform(ratio, 0, 0, ratio, 0, 0);
    this.offsetX = 0;
    this.offsetY = 0;
  }

  /** Draws a grid described by one string per row, using the symbols of the API. */
  render(rows: string[]): void {
    const colours: Record<CellState, string> = {
      T: '#2f7d4f',
      B: '#ff6a1f',
      A: '#3c3a38',
    };
    const size = this.cellSize;
    this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);

    for (let row = 0; row < rows.length; row++) {
      const line = rows[row];
      for (let column = 0; column < line.length; column++) {
        const state = line.charAt(column) as CellState;
        this.context.fillStyle = this.colourOf(colours, state, row, column);
        this.context.fillRect(
          this.offsetX + column * size,
          this.offsetY + row * size,
          size,
          size,
        );
        if (state === 'B' && size >= 6) {
          // Brighter core to make the fire front stand out.
          this.context.fillStyle = '#ffd23f';
          this.context.fillRect(
            this.offsetX + column * size + size * 0.3,
            this.offsetY + row * size + size * 0.3,
            size * 0.4,
            size * 0.4,
          );
        }
      }
    }
    this.drawGridLines(rows.length, rows.length > 0 ? rows[0].length : 0);
  }

  /** Cell under the mouse pointer, or null when the pointer is outside of the grid. */
  cellAt(event: MouseEvent): Cell | null {
    const bounds = this.canvas.getBoundingClientRect();
    const column = Math.floor((event.clientX - bounds.left - this.offsetX) / this.cellSize);
    const row = Math.floor((event.clientY - bounds.top - this.offsetY) / this.cellSize);
    if (row < 0 || row >= this.height || column < 0 || column >= this.width) {
      return null;
    }
    return { row, column };
  }

  private drawGridLines(rows: number, columns: number): void {
    if (this.cellSize < 8) {
      return;
    }
    this.context.strokeStyle = 'rgba(0, 0, 0, 0.18)';
    this.context.lineWidth = 1;
    this.context.beginPath();
    for (let column = 0; column <= columns; column++) {
      const x = this.offsetX + column * this.cellSize + 0.5;
      this.context.moveTo(x, this.offsetY);
      this.context.lineTo(x, this.offsetY + rows * this.cellSize);
    }
    for (let row = 0; row <= rows; row++) {
      const y = this.offsetY + row * this.cellSize + 0.5;
      this.context.moveTo(this.offsetX, y);
      this.context.lineTo(this.offsetX + columns * this.cellSize, y);
    }
    this.context.stroke();
  }

  /** Slight deterministic shade variation, so that the forest does not look like a flat surface. */
  private colourOf(colours: Record<CellState, string>, state: CellState, row: number, column: number): string {
    const base = colours[state];
    if (state !== 'T') {
      return base;
    }
    const noise = ((row * 73856093) ^ (column * 19349663)) % 7;
    const lightness = 26 + Math.abs(noise) * 2;
    return `hsl(147, 45%, ${lightness}%)`;
  }
}
