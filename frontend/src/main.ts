import { ApiError, fetchConfiguration, runSimulation } from './api.js';
import { HistoryChart } from './chart.js';
import { Playback } from './playback.js';
import { ForestRenderer } from './renderer.js';
import { Cell, SimulationParameters, SimulationResponse, SimulationStep } from './types.js';

const MAX_DIMENSION = 200;

const heightInput = element<HTMLInputElement>('height');
const widthInput = element<HTMLInputElement>('width');
const probabilityInput = element<HTMLInputElement>('probability');
const probabilityValue = element<HTMLElement>('probability-value');
const seedInput = element<HTMLInputElement>('seed');
const speedInput = element<HTMLSelectElement>('speed');
const runButton = element<HTMLButtonElement>('run');
const resetButton = element<HTMLButtonElement>('reset');
const clearFiresButton = element<HTMLButtonElement>('clear-fires');
const randomFiresButton = element<HTMLButtonElement>('random-fires');
const playButton = element<HTMLButtonElement>('play');
const previousButton = element<HTMLButtonElement>('previous');
const nextButton = element<HTMLButtonElement>('next');
const restartButton = element<HTMLButtonElement>('restart');
const timeline = element<HTMLInputElement>('timeline');
const messageBox = element<HTMLElement>('message');
const summaryBox = element<HTMLElement>('summary');
const stepLabel = element<HTMLElement>('step-label');
const treesLabel = element<HTMLElement>('trees');
const burningLabel = element<HTMLElement>('burning');
const ashLabel = element<HTMLElement>('ash');
const burntLabel = element<HTMLElement>('burnt');
const fireCountLabel = element<HTMLElement>('fire-count');

const renderer = new ForestRenderer(element<HTMLCanvasElement>('forest'));
const chart = new HistoryChart(element<HTMLCanvasElement>('history'));
const playback = new Playback(onFrame, onPlaybackStateChange);

/** Cells set on fire in the initial state, as "row:column" keys. */
const initialFires = new Set<string>();
let height = 20;
let width = 40;
let result: SimulationResponse | null = null;
let painting: 'add' | 'remove' | null = null;

function element<T extends HTMLElement>(id: string): T {
  const found = document.getElementById(id);
  if (!found) {
    throw new Error(`Missing element #${id}`);
  }
  return found as T;
}

function keyOf(cell: Cell): string {
  return `${cell.row}:${cell.column}`;
}

function toCell(key: string): Cell {
  const [row, column] = key.split(':').map(Number);
  return { row, column };
}

/** The grid of the initial state, in the same format as the one returned by the API. */
function initialRows(): string[] {
  const rows: string[] = [];
  for (let row = 0; row < height; row++) {
    let line = '';
    for (let column = 0; column < width; column++) {
      line += initialFires.has(`${row}:${column}`) ? 'B' : 'T';
    }
    rows.push(line);
  }
  return rows;
}

/** Back to the edition mode: the user is drawing the cells initially on fire. */
function showInitialState(): void {
  result = null;
  playback.clear();
  renderer.setGridSize(height, width);
  renderer.render(initialRows());
  chart.clear();

  timeline.max = '0';
  timeline.value = '0';
  timeline.disabled = true;
  playButton.disabled = true;
  previousButton.disabled = true;
  nextButton.disabled = true;
  restartButton.disabled = true;

  stepLabel.textContent = 'initial state';
  treesLabel.textContent = String(height * width - initialFires.size);
  burningLabel.textContent = String(initialFires.size);
  ashLabel.textContent = '0';
  burntLabel.textContent = '0 %';
  fireCountLabel.textContent = String(initialFires.size);
  summaryBox.textContent = '';
}

function onFrame(step: SimulationStep, index: number, total: number): void {
  renderer.render(step.rows);
  chart.draw(index);

  timeline.value = String(index);
  stepLabel.textContent = `step ${index} / ${total}`;
  treesLabel.textContent = String(step.trees);
  burningLabel.textContent = String(step.burning);
  ashLabel.textContent = String(step.ash);
  const burnt = (step.ash + step.burning) / (height * width);
  burntLabel.textContent = `${(burnt * 100).toFixed(1)} %`;
}

function onPlaybackStateChange(playing: boolean): void {
  playButton.textContent = playing ? 'Pause' : 'Play';
}

function showMessage(text: string, kind: 'error' | 'info' = 'info'): void {
  messageBox.textContent = text;
  messageBox.className = text ? `message ${kind}` : 'message';
}

function readDimension(input: HTMLInputElement, fallback: number): number {
  const value = Number.parseInt(input.value, 10);
  if (!Number.isFinite(value) || value < 1) {
    return fallback;
  }
  return Math.min(value, MAX_DIMENSION);
}

/** Applies the parameters coming from the configuration file of the back end. */
function applyParameters(parameters: SimulationParameters): void {
  height = parameters.height;
  width = parameters.width;
  heightInput.value = String(height);
  widthInput.value = String(width);
  probabilityInput.value = String(parameters.propagationProbability);
  probabilityValue.textContent = parameters.propagationProbability.toFixed(2);
  seedInput.value = parameters.randomSeed === null ? '' : String(parameters.randomSeed);

  initialFires.clear();
  parameters.initialBurningCells.forEach((cell) => initialFires.add(keyOf(cell)));
  showInitialState();
}

function onDimensionChange(): void {
  height = readDimension(heightInput, height);
  width = readDimension(widthInput, width);
  heightInput.value = String(height);
  widthInput.value = String(width);

  // Fires dropped outside of the new grid would be refused by the back end.
  Array.from(initialFires)
    .map(toCell)
    .filter((cell) => cell.row >= height || cell.column >= width)
    .forEach((cell) => initialFires.delete(keyOf(cell)));

  showInitialState();
}

function toggleCellAt(event: MouseEvent): void {
  const cell = renderer.cellAt(event);
  if (!cell) {
    return;
  }
  if (result) {
    // Touching the grid goes back to the edition of the initial state.
    showInitialState();
  }
  const key = keyOf(cell);
  if (painting === null) {
    painting = initialFires.has(key) ? 'remove' : 'add';
  }
  if (painting === 'add') {
    initialFires.add(key);
  } else {
    initialFires.delete(key);
  }
  renderer.render(initialRows());
  fireCountLabel.textContent = String(initialFires.size);
  burningLabel.textContent = String(initialFires.size);
  treesLabel.textContent = String(height * width - initialFires.size);
}

function randomFires(count: number): void {
  initialFires.clear();
  for (let index = 0; index < count; index++) {
    const row = Math.floor(Math.random() * height);
    const column = Math.floor(Math.random() * width);
    initialFires.add(`${row}:${column}`);
  }
  showInitialState();
}

async function run(): Promise<void> {
  if (initialFires.size === 0) {
    showMessage('Click on the forest to set at least one cell on fire.', 'error');
    return;
  }
  const seed = seedInput.value.trim();
  runButton.disabled = true;
  showMessage('Running the simulation...');

  try {
    const response = await runSimulation({
      height,
      width,
      propagationProbability: Number(probabilityInput.value),
      initialBurningCells: Array.from(initialFires).map(toCell),
      randomSeed: seed === '' ? null : Number(seed),
    });

    result = response;
    renderer.setGridSize(response.parameters.height, response.parameters.width);
    chart.setData(response.steps, response.parameters.height * response.parameters.width);
    playback.load(response);

    timeline.max = String(response.stepCount);
    timeline.disabled = false;
    playButton.disabled = false;
    previousButton.disabled = false;
    nextButton.disabled = false;
    restartButton.disabled = false;

    showMessage('');
    summaryBox.textContent = response.stoppedByMaxSteps
      ? `Run interrupted after ${response.stepCount} steps by the maxSteps safety net.`
      : `The fire went out after ${response.stepCount} steps, `
        + `${(response.burntRatio * 100).toFixed(1)}% of the forest burnt.`;
    playback.play();
  } catch (error) {
    const message = error instanceof ApiError ? error.message : String(error);
    showMessage(message, 'error');
  } finally {
    runButton.disabled = false;
  }
}

async function loadConfiguration(): Promise<void> {
  try {
    applyParameters(await fetchConfiguration());
    showMessage('');
  } catch (error) {
    const message = error instanceof ApiError ? error.message : String(error);
    showMessage(message, 'error');
    showInitialState();
  }
}

runButton.addEventListener('click', run);
resetButton.addEventListener('click', loadConfiguration);
clearFiresButton.addEventListener('click', () => {
  initialFires.clear();
  showInitialState();
});
randomFiresButton.addEventListener('click', () => randomFires(3));

heightInput.addEventListener('change', onDimensionChange);
widthInput.addEventListener('change', onDimensionChange);
probabilityInput.addEventListener('input', () => {
  probabilityValue.textContent = Number(probabilityInput.value).toFixed(2);
});
speedInput.addEventListener('change', () => playback.setSpeed(Number(speedInput.value)));

playButton.addEventListener('click', () => playback.toggle());
previousButton.addEventListener('click', () => playback.previous());
nextButton.addEventListener('click', () => playback.next());
restartButton.addEventListener('click', () => playback.restart());
timeline.addEventListener('input', () => {
  playback.pause();
  playback.seek(Number(timeline.value));
});

const canvas = element<HTMLCanvasElement>('forest');
canvas.addEventListener('mousedown', (event) => {
  painting = null;
  toggleCellAt(event);
});
canvas.addEventListener('mousemove', (event) => {
  if (painting !== null && event.buttons === 1) {
    toggleCellAt(event);
  }
});
window.addEventListener('mouseup', () => {
  painting = null;
});

window.addEventListener('keydown', (event) => {
  if (event.target instanceof HTMLInputElement || event.target instanceof HTMLSelectElement) {
    return;
  }
  if (event.code === 'Space' && !playButton.disabled) {
    event.preventDefault();
    playback.toggle();
  } else if (event.code === 'ArrowRight' && !nextButton.disabled) {
    playback.next();
  } else if (event.code === 'ArrowLeft' && !previousButton.disabled) {
    playback.previous();
  }
});

window.addEventListener('resize', () => {
  renderer.layout();
  if (result) {
    onFrame(result.steps[playback.currentIndex], playback.currentIndex, playback.lastIndex);
  } else {
    renderer.render(initialRows());
  }
});

playback.setSpeed(Number(speedInput.value));
void loadConfiguration();
