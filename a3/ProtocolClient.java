package a3;

import Server.*;
import ray.rml.*;
import ray.rage.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.networking.IGameConnection.ProtocolType;
import ray.networking.*;
import ray.networking.client.*;
import java.util.*;

public class ProtocolClient extends GameConnectionClient{
	private MyGame game;
	private UUID id;
	private Vector<GhostAvatar> ghostAvatars;
	
	public ProtocolClient(InetAddress remAddr, int remPort,
						  ProtocolType pType, MyGame game) throws IOException{ 
		super(remAddr, remPort, pType);
		this.game = game;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new Vector<GhostAvatar>();
	}
	
	@Override
	protected void processPacket(Object msg){
		String strMessage = (String)msg;
		String[] messageTokens = strMessage.split(",");
		if(messageTokens.length > 0){
			if(messageTokens[0].compareTo("join") == 0) {
				// receive join
				// format: join, success or join, failure
				if(messageTokens[1].compareTo("success") == 0){ 
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());
					
					
				}
				if(messageTokens[1].compareTo("failure") == 0){ 
					game.setIsConnected(false);
				} 
			}
			if(messageTokens[0].compareTo("bye") == 0){ 
			 // receive bye
			 // format: bye, remoteId
				UUID ghostID = UUID.fromString(messageTokens[1]);
				removeGhostAvatar(ghostID);
			}
			if ((messageTokens[0].compareTo("sdsm") == 0 ) // receive dsfr
			 || (messageTokens[0].compareTo("create")==0)){ 
			 // format: create, remoteId, x,y,z or dsfr, remoteId, x,y,z
				System.out.println("got into create of protClient");
				UUID ghostID = UUID.fromString(messageTokens[1]);
				Vector3 ghostPosition = Vector3f.createFrom(
				Float.parseFloat(messageTokens[2]),
				Float.parseFloat(messageTokens[3]),
				Float.parseFloat(messageTokens[4]));
				try{ 
					createGhostAvatar(ghostID, ghostPosition);
				}catch (NullPointerException e){ 
					//e.printStackTrace();
					System.out.println("error creating ghost avatar");
				} 
			}
			if(messageTokens[0].compareTo("createGhost") == 0){ // rec. create
			
				System.out.println("create ghost packet arrived to client");
				UUID ghostID=UUID.fromString(messageTokens[1]);
				Vector3 ghostPosition = Vector3f.createFrom(
				Float.parseFloat(messageTokens[2]),
				Float.parseFloat(messageTokens[3]),
				Float.parseFloat(messageTokens[4]));
				//createGhostAvatar(ghostID,ghostPosition);
			} 
			if(messageTokens[0].compareTo("wsds") == 0){ // rec. wants
				UUID ghostID=UUID.fromString(messageTokens[1]);
				Vector3 ghostPosition = game.getPlayerPosition();
				sendDetailsForMessage(ghostID, ghostPosition);
				
			}
			if(messageTokens[0].compareTo("move") == 0) // rec. move
			{
				System.out.println("protocol Client move aka sending move from client");
				UUID ghostID=UUID.fromString(messageTokens[1]);
				Vector3 ghostPosition = Vector3f.createFrom(
				Float.parseFloat(messageTokens[2]),
				Float.parseFloat(messageTokens[3]),
				Float.parseFloat(messageTokens[4]));
				//move position of the ghostID
				updateGhost(ghostID,ghostPosition);
				

			}
		} 
	}
	
	public void updateGhost(UUID ghostID, Vector3 position){
		for (GhostAvatar ghost: ghostAvatars){
			if (ghost.getID().equals(ghostID)){
				System.out.println("found the right ghost to move");
				ghost.setPosition(position);
				game.avatar2N.setLocalPosition(position);
				
			}
			
			
		}
		
		
	}
	
	
	public void sendJoinMessage(){ // format: join, localId
		try{ 
			sendPacket(new String("join," + id.toString()));
			System.out.println(id + " joined");
		}catch (IOException e) { 
			e.printStackTrace();
		} 
	}
	
	public void sendCreateMessage(Vector3 pos) { // format: (create, localId, x,y,z)
		try{ 
			String message = new String("create," + id.toString());
			message += "," + pos.x()+"," + pos.y() + "," + pos.z();
			sendPacket(message);
		}catch (IOException e) { 
			e.printStackTrace();
		} 
	}
	public void sendByeMessage(){
	 	try{ 
			sendPacket(new String("bye," + id.toString()));
			System.out.println(id + " left");
		}catch (IOException e) { 
			e.printStackTrace();
		}  
	}
	
	public void sendDetailsForMessage(UUID remId, Vector3 pos){ 
		try{ 
			String message = new String("dsfr," + id.toString()+","+remId.toString());
			message += "," + pos.x()+"," + pos.y() + "," + pos.z();
			sendPacket(message);
		}catch (IOException e) { 
			e.printStackTrace();
		} 
	}
	
	public void sendMoveMessage(Vector3 pos){
		try{ 
			String message = new String("move," + id.toString());
			message += "," + pos.x()+"," + pos.y() + "," + pos.z();
			sendPacket(message);
		}catch (IOException e) { 
			e.printStackTrace();
		}  
	}
	public void createGhostAvatar(UUID ghostID,Vector3 ghostPosition) {
		
		try{
			GhostAvatar newGhost = new GhostAvatar(ghostID, ghostPosition);
			ghostAvatars.add(newGhost);
			System.out.println("creating ghost avatar of "+ghostID.toString());
			game.addGhostAvatarToGameWorld(newGhost,ghostPosition);
		}catch(IOException e) {
			System.out.println("ehh");
		}
		
	}
	public void removeGhostAvatar(UUID ghostID) {
		ghostAvatars.remove(ghostID);
	}
}
