import { RemoveMethods } from 'core/types';

import { PublicKey } from './PublicKey';

export type ServerAddress = string;

export interface ServerState {
  address: string;
  publicKey: string;
}

export class Server {
  /**
   * The canonical address of the server
   */
  address: ServerAddress;

  /**
   * The public key of the server that can be used to send encrypted messages
   */
  publicKey: PublicKey;

  // NOTE: There is no need to store peers: ServerAddress[] here.
  // As soon as a greeting message arrives, we connect to all peers. The server addresses
  // will be added to the lao state as soon as a lao creation message is received
  // over each connection

  /**
   * Constructs a new server instance
   * @param server The properties of the new server instance
   */
  constructor(server: RemoveMethods<Server>) {
    if (server.address === undefined) {
      throw new Error("Undefined 'address' when creating 'Server'");
    }
    this.address = server.address;

    if (server.publicKey === undefined) {
      throw new Error("Undefined 'publicKey' when creating 'Server'");
    }
    this.publicKey = server.publicKey;
  }

  /**
   * Deserializes a server object
   * @param server The serialized server data
   * @returns A deserialized server instance
   */
  public static fromState(server: ServerState): Server {
    return new Server({
      address: server.address,
      publicKey: new PublicKey(server.publicKey),
    });
  }

  /**
   * Serializes a server instance
   * @returns Serialized server data
   */
  public toState(): ServerState {
    return JSON.parse(JSON.stringify(this));
  }
}
