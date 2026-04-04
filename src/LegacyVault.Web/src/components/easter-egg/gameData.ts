// ─── Types ────────────────────────────────────────────────────────────────────

export type Direction = 'north' | 'south' | 'east' | 'west' | 'up' | 'down';
export type OutputLineType = 'normal' | 'heading' | 'win' | 'error' | 'dim' | 'blank' | 'cmd';

export interface OutputLine {
  text: string;
  type?: OutputLineType;
}

export interface Room {
  id: string;
  name: string;
  description: string;
  exits: Partial<Record<Direction, string>>;
  /**
   * Examine targets for this room. Special values:
   *   '__WIN__'             — triggers the win sequence
   *   '__REVEAL_HIDDEN__'   — reveals hidden items for this room (then shows normal text)
   */
  examine: Record<string, string>;
  firstVisit?: string;
}

export interface Item {
  id: string;
  name: string;
  aliases: string[];
  description: string;
  readable?: string;
  takeable: boolean;
}

export interface GameState {
  currentRoom: string;
  inventory: string[];
  /** Mutable per-room item lists (items move as player takes/drops). */
  roomItems: Record<string, string[]>;
  /** 'roomId:direction' pairs for exits that have been unlocked. */
  unlockedExits: string[];
  /** True once the player enters the correct vault combination. */
  combinationSolved: boolean;
  /** Room ids visited at least once. */
  visited: string[];
  won: boolean;
  moves: number;
}

// ─── Items ────────────────────────────────────────────────────────────────────

export const ITEMS: Record<string, Item> = {
  flashlight: {
    id: 'flashlight',
    name: 'flashlight',
    aliases: ['torch', 'light', 'lv-01', 'lantern'],
    description: 'A heavy metal flashlight, batteries still good. "LV-01" is scratched into the handle.',
    takeable: true,
  },
  keycard: {
    id: 'keycard',
    name: 'keycard',
    aliases: ['card', 'key card', 'access card', 'badge', 'id card'],
    description: 'A white plastic keycard with a purple magnetic stripe. Legacy Vault security clearance.',
    takeable: true,
  },
  encrypted_note: {
    id: 'encrypted_note',
    name: 'encrypted note',
    aliases: ['note', 'laminated card', 'laminated', 'ciphertext', 'paper', 'card'],
    description: 'A laminated card covered in ciphertext. Something is penciled in the margin.',
    readable:
      'The printed text is completely unreadable — fully encrypted. But in the margin, handwritten in pencil:\n\n  "FORTRAN \'57"\n\nSomeone left themselves a hint.',
    takeable: true,
  },
  sticky_note: {
    id: 'sticky_note',
    name: 'sticky note',
    aliases: ['note', 'sticky', 'post-it', 'postit', 'yellow note'],
    description:
      "A bright yellow sticky note from the monitor. Neat handwriting: \"Combo = year FORTRAN was released. Don't forget!! — JT\"",
    readable: "\"Combo = year FORTRAN was released. Don't forget!! — JT\"",
    takeable: true,
  },
};

// ─── Rooms ────────────────────────────────────────────────────────────────────

export const ROOMS: Record<string, Room> = {
  server_room: {
    id: 'server_room',
    name: 'Server Room',
    description:
      'You stand in a humming server room. Rows of rack-mounted hardware fill the space, each winking with status LEDs. Purple LED strips run along the floor, casting an eerie violet glow. A door to the NORTH leads deeper into the facility.',
    exits: { north: 'corridor' },
    examine: {
      servers:
        'Rack after rack of blinking hardware. One unit is labelled "LV-CORE-01 — AES-256-GCM". The encryption keys live in here — but the vault needs a physical key to enter.',
      rack: 'Rack after rack of blinking hardware. "LV-CORE-01 — AES-256-GCM" is stencilled on the front panel.',
      racks: 'Rack after rack of blinking hardware. "LV-CORE-01 — AES-256-GCM" is stencilled on the front panel.',
      hardware: 'Humming servers, cooling fans, blinking status lights. The backbone of Legacy Vault.',
      led: 'The purple LED strips pulse in a slow rhythm. Oddly calming, once you get used to it.',
      leds: 'The purple LED strips pulse in a slow rhythm. Oddly calming.',
      lights: 'Purple LED strips run along the floor. Very on-brand.',
      light: 'Purple LED strips run along the floor. Very on-brand.',
      door: 'A heavy steel door, propped open with a server rack rail. Beyond it: the corridor.',
      floor: 'Raised computer flooring. You can hear the ventilation system humming beneath it.',
      me: "You're an archivist. You look determined.",
      self: "You're an archivist. You look determined.",
    },
  },

  corridor: {
    id: 'corridor',
    name: 'Main Corridor',
    description:
      "A long concrete corridor. Red emergency lighting makes everything look cinematic. The server room is to the SOUTH. A side passage branches EAST toward an office. To the NORTH, a heavy steel door bears a keycard reader — its light blinks red.",
    exits: { south: 'server_room', east: 'admin_office', north: 'antechamber' },
    firstVisit: 'The hum of the servers fades to a low background drone.',
    examine: {
      door: 'A heavy vault security door. A keycard reader glows red beside it.',
      'vault door': 'A heavy vault security door. A keycard reader glows red beside it.',
      'north door': 'A heavy vault security door. A keycard reader glows red beside it.',
      reader: "A keycard reader, currently showing red. It needs a card with the right clearance.",
      'keycard reader': "A keycard reader, currently showing red. It needs a card with the right clearance.",
      tile: '__REVEAL_HIDDEN__A floor tile that doesn\'t quite sit flush. The grout around its edges is disturbed.',
      tiles: "Mostly ordinary concrete tiles. One near the north door doesn't sit flush.",
      'loose tile': "__REVEAL_HIDDEN__A floor tile that doesn't quite sit flush. The grout around its edges is disturbed.",
      floor: "Concrete tiles. One near the north door looks slightly loose.",
      camera: 'A security camera, lens dark. The power indicator is off.",',
      cameras: 'Security cameras near the ceiling, all powered down.',
      light: 'Red emergency lighting casts dramatic shadows.',
      lights: 'Red emergency lighting. Dramatic, but functional.',
      wall: 'Bare concrete. Someone stencilled "LV R&D WING" near the floor, ages ago.',
    },
  },

  admin_office: {
    id: 'admin_office',
    name: 'Admin Office',
    description:
      "A cluttered office. A whiteboard dominates one wall, covered in half-erased architecture diagrams. A monitor glows on the desk — Legacy Vault's admin panel, locked. A bright yellow sticky note clings to the bezel. The corridor is back to the WEST.",
    exits: { west: 'corridor' },
    firstVisit: 'The air smells faintly of coffee and dry-erase markers.',
    examine: {
      whiteboard:
        'Boxes and arrows, half erased. Still legible: "AES-256-GCM", "PBKDF2 key derivation", "vault = category". Someone really cared about this.',
      board: 'Architecture diagrams with labels: "AES-256-GCM", "PBKDF2", "vault". Thorough.',
      diagrams: 'Architecture diagrams. The encryption design is meticulous.',
      monitor: "The Legacy Vault admin panel login screen. Patiently waiting. There's a sticky note on the bezel.",
      screen: "The Legacy Vault admin panel. A login prompt glows on screen. No way in without credentials.",
      desk: "A messy desk: empty coffee cups, tangled cables, a sticky note on the monitor.",
      chair: "A battered office chair, slowly spinning. The occupant left in a hurry.",
      cups: 'Multiple empty coffee cups. Whoever works here runs entirely on caffeine.',
      coffee: 'Empty cups everywhere. The scent lingers.',
    },
  },

  antechamber: {
    id: 'antechamber',
    name: 'Vault Antechamber',
    description:
      'The vault antechamber. The air is cool and utterly still. A brass plaque on the wall reads "What is your legacy?" Beside a massive vault door to the NORTH, a four-digit combination dial waits. The corridor is back to the SOUTH.',
    exits: { south: 'corridor', north: 'vault' },
    firstVisit: 'Something about the stillness here makes you feel like the room is listening.',
    examine: {
      plaque: '"What is your legacy?" Engraved in clean block letters. The brass is freshly polished.',
      'brass plaque': '"What is your legacy?" Engraved in clean block letters. The brass is freshly polished.',
      door: 'The vault door. Solid steel, at least a foot thick. Only the correct combination will open it.',
      'vault door': 'The vault door. Solid steel, at least a foot thick. Only the correct combination will open it.',
      dial: 'A four-digit combination dial. Currently reads 0000. You could ENTER a number to try it.',
      combination: 'The combination lock. ENTER a four-digit number to attempt it.',
      lock: 'The combination lock. ENTER a four-digit year to try it.',
      wall: 'Smooth stone walls. The brass plaque glints in the overhead light.',
      floor: 'Polished concrete. Your reflection is faint but visible.',
    },
  },

  vault: {
    id: 'vault',
    name: 'The Vault',
    description:
      "THE VAULT. You're inside. Rows of illuminated shelves stretch from floor to ceiling, holding pages, notes, recipes, memories — a lifetime catalogued and preserved, all sealed with military-grade encryption. At the center of the room, a terminal pulses with soft purple light. The antechamber is back to the SOUTH.",
    exits: { south: 'antechamber' },
    firstVisit: 'The vault door swings shut behind you with a resonant boom.',
    examine: {
      shelves:
        'Countless entries: notes, recipes, passwords, home inventories, reminders. Every category of a human life, organized and protected.',
      shelf: 'Rows of encrypted entries. A complete life, carefully preserved.',
      pages: "Encrypted entries, row upon row. Someone's entire history, safe here.",
      notes: 'Individual encrypted notes, catalogued by category. A life in data.',
      entries: 'Row after row of encrypted vault entries. Notes, passwords, recipes, memories.',
      terminal: '__WIN__',
      console: '__WIN__',
      screen: '__WIN__',
      monitor: '__WIN__',
      computer: '__WIN__',
      panel: '__WIN__',
    },
  },
};

// ─── Hidden items ─────────────────────────────────────────────────────────────
// Items not listed in a room until an examine target triggers __REVEAL_HIDDEN__.
// Structure: roomId -> { itemId -> revealMessage }

export const HIDDEN_ITEMS: Record<string, Record<string, string>> = {
  corridor: {
    keycard:
      'You wedge the loose tile aside. Beneath the dust, a keycard glints — Legacy Vault security clearance. Someone lost this a long time ago.',
  },
};

// ─── Starting item placement ──────────────────────────────────────────────────

const ROOM_START_ITEMS: Record<string, string[]> = {
  server_room: ['flashlight'],
  corridor: [],
  admin_office: ['sticky_note'],
  antechamber: ['encrypted_note'],
  vault: [],
};

// ─── Initial state ────────────────────────────────────────────────────────────

export function createInitialState(): GameState {
  const roomItems: Record<string, string[]> = {};
  for (const key of Object.keys(ROOMS)) {
    roomItems[key] = [...(ROOM_START_ITEMS[key] ?? [])];
  }
  return {
    currentRoom: 'server_room',
    inventory: [],
    roomItems,
    unlockedExits: [],
    combinationSolved: false,
    visited: [],
    won: false,
    moves: 0,
  };
}
