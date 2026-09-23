/** Types shared with the Java REST API. */

export type CellState = 'T' | 'B' | 'A';

export interface Cell {
  row: number;
  column: number;
}

/** Parameters of a simulation, as defined in the configuration file of the back end. */
export interface SimulationParameters {
  height: number;
  width: number;
  propagationProbability: number;
  initialBurningCells: Cell[];
  maxSteps: number;
  randomSeed: number | null;
}

/** Everything the front end may override for a given run. */
export interface SimulationRequest {
  height?: number;
  width?: number;
  propagationProbability?: number;
  initialBurningCells?: Cell[];
  maxSteps?: number;
  randomSeed?: number | null;
}

/** State of the forest at one step, the grid being one string per row. */
export interface SimulationStep {
  index: number;
  rows: string[];
  trees: number;
  burning: number;
  ash: number;
}

export interface SimulationResponse {
  parameters: SimulationParameters;
  stepCount: number;
  stoppedByMaxSteps: boolean;
  burntCells: number;
  burntRatio: number;
  steps: SimulationStep[];
}
