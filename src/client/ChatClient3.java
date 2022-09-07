package client;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JOptionPane;

import server.ChatServerIF;

public class ChatClient3  extends UnicastRemoteObject implements ChatClient3IF {

	private static final long serialVersionUID = 7468891722773409712L;
	ClientRMIGUI chatGUI;
	private String hostName = "localhost";
	private String serviceName = "GroupChatService";
	private String clientServiceName;
	private String name;
	protected ChatServerIF serverIF;
	protected boolean connectionProblem = false;
	
	/**
	* Construtor de classe,
	* note que pode usar um construtor sobrecarregado com
	* uma porta não passada em argumento para super
	* @throws RemoteException
	*/

	public ChatClient3(ClientRMIGUI aChatGUI, String userName) throws RemoteException {
		super();
		this.chatGUI = aChatGUI;
		this.name = userName;
		this.clientServiceName = "ClientListenService_" + userName;
	}

	/**
	* Registre nosso próprio serviço/interface de escuta
	* procure a interface RMI do servidor e envie nossos detalhes
	* @throws RemoteException
	*/
	
	public void startClient() throws RemoteException {		
		String[] details = {name, hostName, clientServiceName};	

		try {
			Naming.rebind("rmi://" + hostName + "/" + clientServiceName, this);
			serverIF = ( ChatServerIF )Naming.lookup("rmi://" + hostName + "/" + serviceName);	
		} 
		catch (ConnectException  e) {
			JOptionPane.showMessageDialog(
					chatGUI.frame, "The server seems to be unavailable\nPlease try later",
					"Connection problem", JOptionPane.ERROR_MESSAGE);
			connectionProblem = true;
			e.printStackTrace();
		}
		catch(NotBoundException | MalformedURLException me){
			connectionProblem = true;
			me.printStackTrace();
		}
		if(!connectionProblem){
			registerWithServer(details);
		}	
		System.out.println("Client Listen RMI Server is running...\n");
	}

	/**
	* passe nosso nome de usuário, nome de host e nome de serviço RMI para
	* o servidor para registrar interesse em participar do chat
	 * @param details
	 */
	
	public void registerWithServer(String[] details) {		
		try{
			serverIF.passIDentity(this.ref);
			serverIF.registerListener(details);			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	//=====================================================================
	/**
	* Receba uma string do servidor de bate-papo
	* este é o método RMI do cliente, que será utilizado pelo servidor
	* para enviar mensagens para nós
	*/
	
	@Override
	public void messageFromServer(String message) throws RemoteException {
		System.out.println( message );
		chatGUI.textArea.append( message );
		//faz com que o gui exiba o último texto anexado, ou seja, rola para baixo
		chatGUI.textArea.setCaretPosition(chatGUI.textArea.getDocument().getLength());
	}
	
	/**
	* Um método para atualizar a exibição de usuários
	* atualmente conectado ao servidor
	 */
	
	@Override
	public void updateUserList(String[] currentUsers) throws RemoteException {

		if(currentUsers.length < 2){
			chatGUI.privateMsgButton.setEnabled(false);
		}
		chatGUI.userPanel.remove(chatGUI.clientPanel);
		chatGUI.setClientPanel(currentUsers);
		chatGUI.clientPanel.repaint();
		chatGUI.clientPanel.revalidate();
	}

}//fim da classe













