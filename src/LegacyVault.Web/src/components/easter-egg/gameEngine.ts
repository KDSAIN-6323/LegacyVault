import { GameState, OutputLine, Direction, ROOMS, ITEMS, HIDDEN_ITEMS } from './gameData';

// ─── Shorthand builders ───────────────────────────────────────────────────────

const L = (text: string, type?: OutputLine['type']): OutputLine => ({ text, type });
const blank = (): OutputLine => ({ text: '', type: 'blank' });
const heading = (text: string): OutputLine => ({ text, type: 'heading' });
const err = (text: string): OutputLine => ({ text, type: 'error' });
const win = (text: string): OutputLine => ({ text, type: 'win' });
const dim = (text: string): OutputLine => ({ text, type: 'dim' });

export type CommandResult = { state: GameState; lines: OutputLine[] };

const ret = (state: GameState, ...lines: OutputLine[]): CommandResult => ({ state, lines });

// ─── Direction aliases ────────────────────────────────────────────────────────

const DIR_ALIASES: Record<string, Direction> = {
  n: 'north', north: 'north',
  s: 'south', south: 'south',
  e: 'east',  east: 'east',
  w: 'west',  west: 'west',
  u: 'up',    up: 'up',
  d: 'down',  down: 'down',
};

// ─── Item matching ────────────────────────────────────────────────────────────

/** Visible items in the current room. */
function roomVisible(state: GameState): string[] {
  return state.roomItems[state.currentRoom] ?? [];
}

/**
 * Match a player's noun phrase against a list of item ids.
 * Returns the first match (exact name/alias first, then partial).
 */
function matchItem(noun: string, candidates: string[]): string | null {
  const n = noun.toLowerCase().trim();
  if (!n) return null;

  for (const id of candidates) {
    const item = ITEMS[id];
    if (!item) continue;
    if (item.name === n || item.aliases.includes(n)) return id;
  }
  // Partial match fallback
  for (const id of candidates) {
    const item = ITEMS[id];
    if (!item) continue;
    if (item.name.includes(n) || n.includes(item.name)) return id;
    if (item.aliases.some(a => a.includes(n) || n.includes(a))) return id;
  }
  return null;
}

// ─── Room description ─────────────────────────────────────────────────────────

function isExitLocked(state: GameState, dir: Direction): boolean {
  if (state.currentRoom === 'corridor' && dir === 'north') {
    return !state.unlockedExits.includes('corridor:north');
  }
  if (state.currentRoom === 'antechamber' && dir === 'north') {
    return !state.combinationSolved;
  }
  return false;
}

export function describeRoom(state: GameState, firstVisit: boolean): OutputLine[] {
  const room = ROOMS[state.currentRoom];
  const out: OutputLine[] = [heading(room.name.toUpperCase()), blank(), L(room.description)];

  if (firstVisit && room.firstVisit) {
    out.push(blank(), L(room.firstVisit));
  }

  const visible = roomVisible(state);
  if (visible.length > 0) {
    out.push(blank());
    for (const id of visible) {
      const item = ITEMS[id];
      if (item) out.push(dim(`There is a ${item.name} here.`));
    }
  }

  const exitDirs = Object.keys(room.exits) as Direction[];
  if (exitDirs.length > 0) {
    const labels = exitDirs.map(d => (isExitLocked(state, d) ? `${d} (locked)` : d));
    out.push(blank(), dim(`Obvious exits: ${labels.join(', ')}.`));
  }

  return out;
}

// ─── Win sequence ─────────────────────────────────────────────────────────────

function winSequence(): OutputLine[] {
  return [
    blank(),
    L('The terminal screen flickers to life.'),
    blank(),
    win('  ┌─────────────────────────────────────────┐'),
    win('  │   LEGACY VAULT  ·  v1.0.0               │'),
    win('  │                                         │'),
    win('  │   Welcome back, Archivist.              │'),
    win('  │                                         │'),
    win('  │   All entries:   SECURE                 │'),
    win('  │   Encryption:    AES-256-GCM            │'),
    win('  │   Key source:    PBKDF2                 │'),
    win('  │                                         │'),
    win('  │   "What is your legacy?"                │'),
    win('  │                                         │'),
    win('  │              You are.                   │'),
    win('  └─────────────────────────────────────────┘'),
    blank(),
    win('*** YOU HAVE WON ***'),
    blank(),
    win("You've found the heart of Legacy Vault. The archive stands ready."),
    win("Every great vault starts empty — now go fill yours with something worth keeping."),
    blank(),
    dim('Type QUIT to close, or keep exploring.'),
  ];
}

// ─── Handlers ─────────────────────────────────────────────────────────────────

function handleExamine(state: GameState, noun: string): CommandResult {
  const room = ROOMS[state.currentRoom];

  // 1. Check room examine targets first
  const examKey = Object.keys(room.examine).find(k =>
    noun === k || noun.includes(k) || k.includes(noun),
  );

  if (examKey) {
    const val = room.examine[examKey];

    if (val === '__WIN__') {
      return { state: { ...state, won: true }, lines: winSequence() };
    }

    if (val.startsWith('__REVEAL_HIDDEN__')) {
      const displayText = val.slice('__REVEAL_HIDDEN__'.length).trim();
      const hidden = HIDDEN_ITEMS[state.currentRoom] ?? {};
      let ns = state;
      const revealLines: OutputLine[] = [];

      for (const [itemId, revealMsg] of Object.entries(hidden)) {
        if (!(state.roomItems[state.currentRoom] ?? []).includes(itemId)) {
          ns = {
            ...ns,
            roomItems: {
              ...ns.roomItems,
              [ns.currentRoom]: [...(ns.roomItems[ns.currentRoom] ?? []), itemId],
            },
          };
          revealLines.push(blank(), L(revealMsg));
        }
      }

      return {
        state: ns,
        lines: [L(displayText), ...revealLines],
      };
    }

    return ret(state, L(val));
  }

  // 2. Self-examination
  if (['self', 'me', 'myself', 'you', 'player', 'archivist'].includes(noun)) {
    return ret(state, L("You're the archivist. Looking determined. Possibly caffeinated."));
  }

  // 3. Items in room or inventory
  const inRoom = matchItem(noun, roomVisible(state));
  if (inRoom) return ret(state, L(ITEMS[inRoom].description));

  const inInv = matchItem(noun, state.inventory);
  if (inInv) return ret(state, L(ITEMS[inInv].description));

  return ret(state, err(`You don't see any ${noun} here.`));
}

function handleRead(state: GameState, noun: string): CommandResult {
  const candidates = [...roomVisible(state), ...state.inventory];
  const itemId = matchItem(noun, candidates);
  if (!itemId) return ret(state, err(`You don't see any ${noun} here.`));

  const item = ITEMS[itemId];
  if (!item.readable) return ret(state, L(`There's nothing written on the ${item.name}.`));
  return ret(state, L(item.readable));
}

function handleTake(state: GameState, noun: string): CommandResult {
  const inInv = matchItem(noun, state.inventory);
  if (inInv) return ret(state, L(`You already have the ${ITEMS[inInv].name}.`));

  const inRoom = matchItem(noun, roomVisible(state));
  if (!inRoom) return ret(state, err(`You don't see any ${noun} here.`));

  const item = ITEMS[inRoom];
  if (!item.takeable) return ret(state, err(`You can't take the ${item.name}.`));

  const roomId = state.currentRoom;
  return {
    state: {
      ...state,
      inventory: [...state.inventory, inRoom],
      roomItems: {
        ...state.roomItems,
        [roomId]: (state.roomItems[roomId] ?? []).filter(id => id !== inRoom),
      },
    },
    lines: [L('Taken.')],
  };
}

function handleDrop(state: GameState, noun: string): CommandResult {
  const invId = matchItem(noun, state.inventory);
  if (!invId) return ret(state, err(`You aren't carrying any ${noun}.`));

  const roomId = state.currentRoom;
  return {
    state: {
      ...state,
      inventory: state.inventory.filter(id => id !== invId),
      roomItems: {
        ...state.roomItems,
        [roomId]: [...(state.roomItems[roomId] ?? []), invId],
      },
    },
    lines: [L(`You set down the ${ITEMS[invId].name}.`)],
  };
}

function doMove(state: GameState, dir: Direction, preMsg?: string): CommandResult {
  const room = ROOMS[state.currentRoom];
  const dest = room.exits[dir]!;
  const isFirst = !state.visited.includes(dest);

  const newState: GameState = {
    ...state,
    currentRoom: dest,
    visited: isFirst ? [...state.visited, dest] : state.visited,
  };

  const lines: OutputLine[] = [];
  if (preMsg) lines.push(L(preMsg), blank());
  lines.push(...describeRoom(newState, isFirst));

  return { state: newState, lines };
}

function handleMove(state: GameState, dir: Direction): CommandResult {
  const room = ROOMS[state.currentRoom];
  if (!room.exits[dir]) {
    return ret(state, err(`You can't go ${dir} from here.`));
  }

  // Keycard door
  if (state.currentRoom === 'corridor' && dir === 'north') {
    if (!state.unlockedExits.includes('corridor:north')) {
      if (state.inventory.includes('keycard')) {
        const unlocked = {
          ...state,
          unlockedExits: [...state.unlockedExits, 'corridor:north'],
        };
        return doMove(
          unlocked,
          dir,
          'You hold the keycard to the reader. It beeps twice. The red light turns green — the door clicks open.',
        );
      }
      return ret(state, err("The keycard reader blinks red. Access denied. You'll need a keycard."));
    }
  }

  // Combination door
  if (state.currentRoom === 'antechamber' && dir === 'north') {
    if (!state.combinationSolved) {
      return ret(
        state,
        L("The vault door stands firm. The combination dial awaits. (ENTER a four-digit year to try it.)"),
      );
    }
  }

  return doMove(state, dir);
}

function handleUseKeycard(state: GameState): CommandResult {
  if (state.currentRoom !== 'corridor') {
    return ret(state, L("There's no keycard reader here."));
  }
  if (!state.inventory.includes('keycard')) {
    return ret(state, err("You don't have the keycard."));
  }
  if (state.unlockedExits.includes('corridor:north')) {
    return ret(state, L('The door is already unlocked.'));
  }
  return {
    state: { ...state, unlockedExits: [...state.unlockedExits, 'corridor:north'] },
    lines: [
      L('You hold the keycard to the reader. It beeps twice. The red light turns green — the heavy door clicks open.'),
      blank(),
      dim('The north exit is now open.'),
    ],
  };
}

function handleUse(state: GameState, rest: string): CommandResult {
  let itemNoun = rest;
  const prepMatch = rest.match(/^(.+?)\s+(?:on|with|at|against)\s+(.+)$/);
  if (prepMatch) itemNoun = prepMatch[1].trim();

  const itemId = matchItem(itemNoun, state.inventory);
  if (!itemId) {
    const inRoom = matchItem(itemNoun, roomVisible(state));
    if (inRoom) return ret(state, L(`You'll need to take the ${ITEMS[inRoom].name} first.`));
    return ret(state, err(`You aren't carrying any ${itemNoun}.`));
  }

  if (itemId === 'keycard') return handleUseKeycard(state);

  if (itemId === 'flashlight') {
    return ret(
      state,
      L("You click the flashlight on. The beam sweeps around. Nothing new is illuminated, but it's satisfying."),
    );
  }

  return ret(state, L(`You're not sure how to use the ${ITEMS[itemId].name} here.`));
}

function handleUnlock(state: GameState, rest: string): CommandResult {
  let targetNoun = rest;
  let toolNoun = '';

  const withMatch = rest.match(/^(.+?)\s+(?:with|using)\s+(.+)$/);
  if (withMatch) {
    targetNoun = withMatch[1].trim();
    toolNoun = withMatch[2].trim();
  }

  if (toolNoun) {
    const toolId = matchItem(toolNoun, state.inventory);
    if (!toolId) return ret(state, err(`You aren't carrying any ${toolNoun}.`));
    if (toolId === 'keycard') return handleUseKeycard(state);
    return ret(state, err(`The ${ITEMS[toolId].name} doesn't work here.`));
  }

  const wantsNorth =
    !targetNoun ||
    targetNoun.includes('north') ||
    targetNoun.includes('door') ||
    targetNoun.includes('vault');

  if (wantsNorth) {
    if (state.currentRoom === 'corridor') return handleUseKeycard(state);
    if (state.currentRoom === 'antechamber') {
      return ret(state, L('The vault door needs a combination, not a key. ENTER a four-digit number.'));
    }
  }

  return ret(state, err("There's nothing obvious to unlock here."));
}

function handleEnter(state: GameState, input: string): CommandResult {
  const numMatch = input.match(/\b(\d{3,4})\b/);

  if (!numMatch) {
    if (state.currentRoom === 'antechamber') {
      return ret(state, L('The combination dial reads 0000. Enter a four-digit year.'));
    }
    return ret(state, err('Enter what?'));
  }

  const number = numMatch[1];

  if (state.currentRoom !== 'antechamber') {
    return ret(state, err("There's no combination lock here."));
  }
  if (state.combinationSolved) {
    return ret(state, L('The vault door is already open.'));
  }

  if (number === '1957') {
    return {
      state: { ...state, combinationSolved: true },
      lines: [
        L('You slowly turn the dial: 1... 9... 5... 7.'),
        blank(),
        L('A deep mechanical CLUNK resonates through the steel. The vault door swings open, releasing a breath of cool, conditioned air.'),
        blank(),
        dim('The north exit is now open.'),
      ],
    };
  }

  return ret(
    state,
    L(`You turn the dial to ${number}. Nothing happens. The combination snaps back to 0000.`),
  );
}

// ─── Main entry point ─────────────────────────────────────────────────────────

export function processCommand(state: GameState, rawInput: string): CommandResult {
  const input = rawInput.trim().toLowerCase();
  if (!input) return { state, lines: [] };

  const ns: GameState = { ...state, moves: state.moves + 1 };
  const words = input.split(/\s+/);
  const verb = words[0];
  const rest = words.slice(1).join(' ');

  // ── QUIT ──────────────────────────────────────────────────────────────────
  if (['quit', 'exit', 'q', 'bye'].includes(verb) && !rest) {
    return ret({ ...ns, won: true }, L('Farewell, Archivist. The vault remembers.'));
  }

  // ── HELP ──────────────────────────────────────────────────────────────────
  if (['help', '?', 'h'].includes(verb) || input === 'help me') {
    return ret(
      ns,
      heading('COMMANDS'),
      blank(),
      L('LOOK (L)              — Describe your surroundings'),
      L('GO [direction]        — Move (or just type N, S, E, W)'),
      L('EXAMINE (X) [thing]   — Look closely at something'),
      L('TAKE [item]           — Pick up an item'),
      L('DROP [item]           — Put an item down'),
      L('INVENTORY (I)         — List what you\'re carrying'),
      L('READ [item]           — Read a document'),
      L('USE [item]            — Use an item'),
      L('UNLOCK [door]         — Unlock a door (if you have the means)'),
      L('ENTER [number]        — Enter a combination on a lock'),
      L('SCORE                 — Show your move count'),
      L('QUIT                  — Close the adventure'),
    );
  }

  // ── SCORE ─────────────────────────────────────────────────────────────────
  if (verb === 'score' || verb === 'moves') {
    return ret(ns, L(`Moves made: ${state.moves}.`));
  }

  // ── INVENTORY ─────────────────────────────────────────────────────────────
  if (['inventory', 'inv', 'i'].includes(verb) && !rest) {
    if (state.inventory.length === 0) return ret(ns, L('You are carrying nothing.'));
    return ret(
      ns,
      heading('YOU ARE CARRYING'),
      blank(),
      ...state.inventory.map(id => L(`  ${ITEMS[id]?.name ?? id}`)),
    );
  }

  // ── LOOK ──────────────────────────────────────────────────────────────────
  if ((verb === 'look' || verb === 'l') && (!rest || rest === 'around' || rest === 'here')) {
    return ret(ns, ...describeRoom(ns, false));
  }

  // ── EXAMINE ───────────────────────────────────────────────────────────────
  const isExamine =
    ['examine', 'x', 'inspect', 'describe'].includes(verb) ||
    (verb === 'look' && words[1] === 'at') ||
    input.startsWith('look at ');

  if (isExamine) {
    let noun = rest;
    if (verb === 'look' && words[1] === 'at') noun = words.slice(2).join(' ');
    if (input.startsWith('look at ')) noun = input.slice('look at '.length);
    noun = noun.trim();
    if (!noun) return ret(ns, ...describeRoom(ns, false));
    return handleExamine(ns, noun);
  }

  // ── READ ──────────────────────────────────────────────────────────────────
  if (verb === 'read') {
    if (!rest) return ret(ns, err('Read what?'));
    return handleRead(ns, rest);
  }

  // ── TAKE / GET ────────────────────────────────────────────────────────────
  if (['take', 'get', 'grab', 'pick'].includes(verb) || input.startsWith('pick up ')) {
    let noun = rest;
    if (input.startsWith('pick up ')) noun = input.slice('pick up '.length);
    else if (verb === 'pick' && words[1] === 'up') noun = words.slice(2).join(' ');
    noun = noun.trim();
    if (!noun) return ret(ns, err('Take what?'));
    return handleTake(ns, noun);
  }

  // ── DROP ──────────────────────────────────────────────────────────────────
  if (['drop', 'discard', 'leave'].includes(verb) || input.startsWith('put down ')) {
    const noun = (input.startsWith('put down ') ? input.slice('put down '.length) : rest).trim();
    if (!noun) return ret(ns, err('Drop what?'));
    return handleDrop(ns, noun);
  }

  // ── MOVEMENT ──────────────────────────────────────────────────────────────
  let moveDir: Direction | null = null;
  if (DIR_ALIASES[verb] && !rest) {
    moveDir = DIR_ALIASES[verb];
  } else if (['go', 'walk', 'move', 'head', 'run', 'travel'].includes(verb) && rest) {
    moveDir = DIR_ALIASES[rest.trim()] ?? null;
  }
  if (moveDir) return handleMove(ns, moveDir);

  // ── USE ───────────────────────────────────────────────────────────────────
  if (['use', 'apply', 'swipe', 'activate'].includes(verb)) {
    return handleUse(ns, rest);
  }

  // ── UNLOCK / OPEN ─────────────────────────────────────────────────────────
  if (['unlock', 'open'].includes(verb)) {
    return handleUnlock(ns, rest);
  }

  // ── ENTER / DIAL (combination) ────────────────────────────────────────────
  if (['enter', 'type', 'dial', 'set', 'input', 'turn', 'spin', 'rotate', 'try'].includes(verb)) {
    return handleEnter(ns, input);
  }

  // ── Flavour responses ─────────────────────────────────────────────────────
  if (['xyzzy', 'plugh', 'plover', 'abracadabra'].includes(input)) {
    return ret(ns, dim('A hollow voice says "FOOL."'));
  }
  if (input === 'hello' || input === 'hi') {
    return ret(ns, L('The facility greets you with the hum of cooling fans.'));
  }
  if (input === 'wait' || input === 'z') {
    return ret(ns, L('Time passes.'));
  }
  if (['scream', 'yell', 'shout'].includes(verb)) {
    return ret(ns, L('Your voice echoes down the corridor. No response.'));
  }
  if (['dance', 'jump', 'spin'].includes(verb)) {
    return ret(ns, L("This is no time for that. Or maybe it is — no one is watching."));
  }
  if (verb === 'sleep' || verb === 'rest') {
    return ret(ns, L("You're inside an encrypted vault facility. Sleep later."));
  }

  // ── Unknown ───────────────────────────────────────────────────────────────
  return ret(ns, err(`I don't understand "${words[0]}". (Type HELP for a list of commands.)`));
}
