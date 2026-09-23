import { SimulationResponse, SimulationStep } from './types.js';

/** Plays the steps of a simulation like a video: play, pause, step by step and seek. */
export class Playback {
  private steps: SimulationStep[] = [];
  private index = 0;
  private timer: number | null = null;
  private stepsPerSecond = 8;

  constructor(
    private readonly onFrame: (step: SimulationStep, index: number, total: number) => void,
    private readonly onStateChange: (playing: boolean) => void,
  ) {}

  load(response: SimulationResponse): void {
    this.pause();
    this.steps = response.steps;
    this.index = 0;
    this.emit();
  }

  /** Forgets the loaded run, for instance when the user goes back to editing the initial state. */
  clear(): void {
    this.pause();
    this.steps = [];
    this.index = 0;
  }

  get currentIndex(): number {
    return this.index;
  }

  get lastIndex(): number {
    return Math.max(0, this.steps.length - 1);
  }

  get isPlaying(): boolean {
    return this.timer !== null;
  }

  setSpeed(stepsPerSecond: number): void {
    this.stepsPerSecond = stepsPerSecond;
    if (this.isPlaying) {
      this.play();
    }
  }

  play(): void {
    if (this.steps.length === 0) {
      return;
    }
    if (this.index >= this.lastIndex) {
      this.index = 0;
    }
    this.stopTimer();
    this.timer = window.setInterval(() => {
      if (this.index >= this.lastIndex) {
        this.pause();
        return;
      }
      this.index++;
      this.emit();
    }, 1000 / this.stepsPerSecond);
    this.onStateChange(true);
  }

  pause(): void {
    this.stopTimer();
    this.onStateChange(false);
  }

  toggle(): void {
    if (this.isPlaying) {
      this.pause();
    } else {
      this.play();
    }
  }

  seek(index: number): void {
    if (this.steps.length === 0) {
      return;
    }
    this.index = Math.min(Math.max(index, 0), this.lastIndex);
    this.emit();
  }

  next(): void {
    this.pause();
    this.seek(this.index + 1);
  }

  previous(): void {
    this.pause();
    this.seek(this.index - 1);
  }

  restart(): void {
    this.pause();
    this.seek(0);
  }

  private stopTimer(): void {
    if (this.timer !== null) {
      window.clearInterval(this.timer);
      this.timer = null;
    }
  }

  private emit(): void {
    const step = this.steps[this.index];
    if (step) {
      this.onFrame(step, this.index, this.lastIndex);
    }
  }
}
