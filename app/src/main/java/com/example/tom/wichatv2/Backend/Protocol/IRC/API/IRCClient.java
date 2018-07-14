package com.example.tom.wichatv2.Backend.Protocol.IRC.API;


import android.util.Log;

import com.example.tom.wichatv2.Backend.Domain.API.Chatroom;
import com.example.tom.wichatv2.Backend.Domain.API.User;
import com.example.tom.wichatv2.Backend.Domain.API.UserFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;


/**
 * PircBot is a Java framework for writing IRC bots quickly and easily.
 * <p/>
 * It provides an event-driven architecture to handle common IRC
 * events, flood protection, DCC support, ident support, and more.
 * The comprehensive logfile format is suitable for use with pisg to generate
 * channel statistics.
 * <p/>
 * Methods of the PircBot class can be called to send events to the IRC server
 * that it connects to.  For example, calling the sendMessage method will
 * send a message to a channel or user on the IRC server.  Multiple servers
 * can be supported using multiple instances of PircBot.
 * <p/>
 * To perform an action when the PircBot receives a normal message from the IRC
 * server, you would override the onMessage method defined in the PircBot
 * class.  All on<i>XYZ</i> methods in the PircBot class are automatically called
 * when the event <i>XYZ</i> happens, so you would override these if you wish
 * to do something when it does happen.
 * <p/>
 * Some event methods, such as onPing, should only really perform a specific
 * function (i.e. respond to a PING from the server).  For your convenience, such
 * methods are already correctly implemented in the PircBot and should not
 * normally need to be overridden.  Please read the full documentation for each
 * method to see which ones are already implemented by the PircBot class.
 * <p/>
 * Please visit the PircBot homepage at
 * <a href="http://www.jibble.org/pircbot.php">http://www.jibble.org/pircbot.php</a>
 * for full revision history, a beginners guide to creating your first PircBot
 * and a list of some existing Java IRC bots and clients that use the PircBot
 * framework.
 *
 * @author Paul James Mutton,
 * <a href="http://www.jibble.org/">http://www.jibble.org/</a>
 * @version 1.4.6 (Build time: Wed Apr 11 19:20:59 2007)
 */
public abstract class IRCClient implements ReplyConstants {
    /**
     * The definitive version number of this release of PircBot.
     * (Note: Change this before automatically building releases)
     */
    public static final String VERSION = "1.4.6";

    private static final int OP_ADD = 1;
    private static final int OP_REMOVE = 2;
    private static final int VOICE_ADD = 3;
    private static final int VOICE_REMOVE = 4;
    private static final int HALFOP_ADD = 5;
    private static final int HALFOP_REMOVE = 6;
    protected UserFactory userFactory;
    protected Chatroom chatroom;

    /**
     * Constructs a PircBot with the default settings.  Your own constructors
     * in classes which extend the PircBot abstract class should be responsible
     * for changing the default settings if required.
     */
    public IRCClient(UserFactory userFactory, Chatroom chatroom) {
        this.userFactory = userFactory;
        this.chatroom = chatroom;
    }

    /**
     * Attempt to connect to the specified IRC server.
     * The onConnect method is called upon success.
     *
     * @param hostname The hostname of the server to connect to.
     * @throws IOException               if it was not possible to connect to the server.
     * @throws IRCException              if the server would not let us join it.
     * @throws NickAlreadyInUseException if our nick is already in use on the server.
     */
    public final synchronized void connect(String hostname, String username) throws IOException, com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCException {
        this.connect(hostname, 6667, null, username);
    }


    /**
     * Attempt to connect to the specified IRC server and port number.
     * The onConnect method is called upon success.
     *
     * @param hostname The hostname of the server to connect to.
     * @param port     The port number to connect to on the server.
     * @throws IOException                                                                 if it was not possible to connect to the server.
     * @throws com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCException              if the server would not let us join it.
     * @throws com.example.tom.wichatv2.Backend.Protocol.IRC.API.NickAlreadyInUseException if our nick is already in use on the server.
     */
    public synchronized void connect(String hostname, int port, String username) throws IOException, com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCException {
        this.connect(hostname, port, null, username);
    }


    /**
     * Attempt to connect to the specified IRC server using the supplied
     * password.
     * The onConnect method is called upon success.
     *
     * @param hostname The hostname of the server to connect to.
     * @param port     The port number to connect to on the server.
     * @param password The password to use to join the server.
     * @throws IOException                                                                 if it was not possible to connect to the server.
     * @throws com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCException              if the server would not let us join it.
     * @throws com.example.tom.wichatv2.Backend.Protocol.IRC.API.NickAlreadyInUseException if our nick is already in use on the server.
     */
    private synchronized void connect(String hostname, int port, String password, String username) throws IOException, com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCException {
        _registered = false;
        _nick = username;
        _name = username;
        _server = hostname;
        _port = port;
        _password = password;

        if (isConnected()) {
            throw new IOException("Simple IRC is already connected to an IRC server.  Disconnect first.");
        }
        _autoNickTries = 1;

        // Don't clear the outqueue - there might be something important in it!

        // Clear everything we may have know about channels.
        // this.removeAllChannels();

        // Connect to the server.

        try {
            // XXX: PircBot Patch for SSL
            if (_useSSL) {
                try {
                    this.onConnectionMessage("Opening SSL connection...");
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, _trustManagers, new SecureRandom());
                    SSLSocketFactory factory = context.getSocketFactory();
                    SSLSocket ssocket;
                    try {
                        ssocket = (SSLSocket) factory.createSocket(hostname, port);
                    } catch (Exception exx) {
                        throw new IOException("Error opening socket: " + exx.toString(), exx);
                    }
                    try {
                        ssocket.startHandshake();
                    } catch (Exception exx) {
                        throw new SSLException("SSL handshake failed: " + exx.toString(), exx);
                    }
                    _socket = ssocket;
                } catch (Exception e) {
                    throw new SSLException("SSL connection failed: " + e.getMessage(), e);
                }

            } else {
                try {
                    _socket = new Socket(hostname, port);
                } catch (Exception exx) {
                    throw new IOException("Error opening socket: " + exx.toString() + " " + hostname + " " + port, exx);
                }
            }
        } catch (IOException ex) {
            throw new IOException("Problem connecting to server: " + ex.toString());
        }

        // This makes the socket timeout on read operations after 5 minutes.
        // Maybe in some future version I will let the user change this at runtime.
        _socket.setSoTimeout(60 * 1000);


        _inetAddress = _socket.getLocalAddress();

        InputStreamReader inputStreamReader;
        OutputStreamWriter outputStreamWriter;

        if (getEncoding() != null) {
            // Assume the specified encoding is valid for this JVM.
            inputStreamReader = new InputStreamReader(_socket.getInputStream(), getEncoding());
            outputStreamWriter = new OutputStreamWriter(_socket.getOutputStream(), getEncoding());
        } else {
            // Otherwise, just use the JVM's default encoding.
            inputStreamReader = new InputStreamReader(_socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(_socket.getOutputStream());
        }

        BufferedReader breader = new BufferedReader(inputStreamReader);
        BufferedWriter bwriter = new BufferedWriter(outputStreamWriter);


        // Now start the outputThread that will be used to send all messages.
        if (_outputThread == null) {
            _outputThread = new OutputThread(this, _outQueue);
            _outputThread.start();
        }


        // Set up the input thread.
        _inputThread = new InputThread(this, _socket, breader, bwriter);
        // Now start the InputThread to read all other lines from the server.
        _inputThread.start();


        // Attempt to join the server.
        if (password != null && !password.equals("")) {
            sendRawLine("PASS " + password);
        }
        String nick = this.getName();


        // XXX: PircBot Patch - Set nick before loop. otherwise we overwrite it in the loop again and again
        //                      But maybe we got a new nickname from the server (bouncers!)
        this.setNick(nick);


        // Read stuff back from the server to see if we connected.
        String line;
        line = breader.readLine();

        // XXX: PircBot patch - We are not connected to server if nothing received
        if (line == null) {
            throw new IOException("Could not connect to server " + hostname);
        }

        // Send the first line to handleLine before the InputThread is started.
        this.handleLine(line);
        sendRawLine("USER " + this.getName() + " 0 * :" + this.getVersion());
        sendRawLine("NICK " + this.getName());
        this.onConnect();
    }


    /**
     * Reconnects to the IRC server that we were previously connected to.
     * If necessary, the appropriate port number and password will be used.
     * This method will throw an IRCException if we have never connected
     * to an IRC server previously.
     *
     * @throws IOException                                                                 if it was not possible to connect to the server.
     * @throws com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCException              if the server would not let us join it.
     * @throws com.example.tom.wichatv2.Backend.Protocol.IRC.API.NickAlreadyInUseException if our nick is already in use on the server.
     * @since PircBot 0.9.9
     */
    public final synchronized void reconnect() throws IOException, com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCException {
        if (getServer() == null) {
            throw new com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCException("Cannot reconnect to an IRC server because we were never connected to one previously!");
        }
        connect(getServer(), getPort(), getPassword());
    }

    /**
     * Set wether SSL should be used to connect to the server.
     *
     * @author Sebastian Kaspari <sebastian@yaaic.org>
     */
    public void setUseSSL(boolean useSSL) {
        _useSSL = useSSL;
    }


    /**
     * This method disconnects from the server cleanly by calling the
     * quitServer() method.  Providing the PircBot was connected to an
     * IRC server, the onDisconnect() will be called as soon as the
     * disconnection is made by the server.
     *
     * @see #quitServer() quitServer
     * @see #quitServer(String) quitServer
     */
    public final synchronized void disconnect() {
        this.quitServer();
    }


    /**
     * When you connect to a server and your nick is already in use and
     * this is set to true, a new nick will be automatically chosen.
     * This is done by adding numbers to the end of the nick until an
     * available nick is found.
     *
     * @param autoNickChange Set to true if you want automatic nick changes
     *                       during connection.
     */
    public void setAutoNickChange(boolean autoNickChange) {
        _autoNickChange = autoNickChange;
    }

    /**
     * Joins a channel.
     *
     * @param channel The name of the channel to join (eg "#cs").
     */
    public final void joinChannel(String channel) {
        this.sendRawLine("JOIN " + channel);
    }


    /**
     * Joins a channel with a key.
     *
     * @param channel The name of the channel to join (eg "#cs").
     * @param key     The key that will be used to join the channel.
     */
    public final void joinChannel(String channel, String key) {
        this.joinChannel(channel + " " + key);
    }


    /**
     * Parts a channel.
     *
     * @param channel The name of the channel to leave.
     */
    public final void partChannel(String channel) {
        _outQueue.addFront("part " + channel);
    }


    /**
     * Parts a channel, giving a reason.
     *
     * @param channel The name of the channel to leave.
     * @param reason  The reason for parting the channel.
     */
    public final void partChannel(String channel, String reason) {
        _outQueue.addFront("PART " + channel + " :" + reason);
    }


    /**
     * Quits from the IRC server.
     * Providing we are actually connected to an IRC server, the
     * onDisconnect() method will be called as soon as the IRC server
     * disconnects us.
     */
    public void quitServer() {
        this.quitServer("");
    }


    /**
     * Quits from the IRC server with a reason.
     * Providing we are actually connected to an IRC server, the
     * onDisconnect() method will be called as soon as the IRC server
     * disconnects us.
     *
     * @param reason The reason for quitting the server.
     */
    // XXX PircBot patch -- we need to override this method in Simple IRC
    public void quitServer(String reason) {
        this.sendRawLine("QUIT :" + reason);
    }


    /**
     * Sends a raw line to the IRC server as soon as possible, bypassing the
     * outgoing message queue.
     *
     * @param line The raw line to send to the IRC server.
     */
    public final void sendRawLine(String line) {
        if (_inputThread == null || _outputThread == null) {
            Log.d("pIRCbot", "Tried sending message on null threads?");
            return;
        }
        _inputThread.sendRawLine(line);
    }

    /**
     * Sends a raw line through the outgoing message queue.
     *
     * @param line The raw line to send to the IRC server.
     */
    final synchronized void sendRawLineViaQueue(String line) {
        if (line == null) {
            throw new NullPointerException("Cannot send null messages to server");
        }
        if (isConnected()) {
            _outQueue.add(line);
        } else {
            // We're not yet connected. This should be shoved right onto the stack.
            this._inputThread.sendRawLine(line);
        }
    }


    /**
     * Sends a message to a channel or a private message to a user.  These
     * messages are added to the outgoing message queue and sent at the
     * earliest possible opportunity.
     * <p>
     * You may optionally apply colours, boldness, underlining, etc to
     * the message by using the <code>Colors</code> class.
     *
     * @param target  The name of the channel or user nick to send to.
     * @param message The message to send.
     */
    protected void sendMessage(String target, String message) {
        // Send message here
        _outQueue.add("PRIVMSG " + target + " :" + message);
    }


    /**
     * Sends an action to the channel or to a user.
     *
     * @param target The name of the channel or user nick to send to.
     * @param action The action to send.
     */
    public final void sendAction(String target, String action) {

        // Send CTCP ACTION here
        sendCTCPCommand(target, "ACTION " + action);
    }


    /**
     * Sends a notice to the channel or to a user.
     *
     * @param target The name of the channel or user nick to send to.
     * @param notice The notice to send.
     */
    public final void sendNotice(String target, String notice) {
        _outQueue.add("NOTICE " + target + " :" + notice);
    }


    /**
     * Sends a CTCP command to a channel or user.  (Client to client protocol).
     * Examples of such commands are "PING <number>", "FINGER", "VERSION", etc.
     * For example, if you wish to request the version of a user called "Dave",
     * then you would call <code>sendCTCPCommand("Dave", "VERSION");</code>.
     * The type of response to such commands is largely dependant on the target
     * client software.
     *
     * @param target  The name of the channel or user to send the CTCP message to.
     * @param command The CTCP command to send.
     * @since PircBot 0.9.5
     */
    private void sendCTCPCommand(String target, String command) {
        _outQueue.add("PRIVMSG " + target + " :\u0001" + command + "\u0001");
    }


    /**
     * Attempt to change the current nick (nickname) of the bot when it
     * is connected to an IRC server.
     * After confirmation of a successful nick change, the getNick method
     * will return the new nick.
     *
     * @param newNick The new nick to use.
     */
    public final void changeNick(String newNick) {
        this.sendRawLine("NICK " + newNick);
    }


    /**
     * Identify the bot with NickServ, supplying the appropriate password.
     * Some IRC Networks (such as freenode) require users to <i>register</i> and
     * <i>identify</i> with NickServ before they are able to send private messages
     * to other users, thus reducing the amount of spam.  If you are using
     * an IRC network where this kind of policy is enforced, you will need
     * to make your bot <i>identify</i> itself to NickServ before you can send
     * private messages. Assuming you have already registered your bot's
     * nick with NickServ, this method can be used to <i>identify</i> with
     * the supplied password. It usually makes sense to identify with NickServ
     * immediately after connecting to a server.
     * <p/>
     * This method issues a raw NICKSERV command to the server, and is therefore
     * safer than the alternative approach of sending a private message to
     * NickServ. The latter approach is considered dangerous, as it may cause
     * you to inadvertently transmit your password to an untrusted party if you
     * connect to a network which does not run a NickServ service and where the
     * untrusted party has assumed the nick "NickServ".  However, if your IRC
     * network is only compatible with the private message approach, you may
     * typically identify like so:
     * <pre>sendMessage("NickServ", "identify PASSWORD");</pre>
     *
     * @param password The password which will be used to identify with NickServ.
     */
    public final void identify(String password) {
        this.sendMessage("NickServ", "identify " + password);
    }


    /**
     * Set the mode of a channel.
     * This method attempts to set the mode of a channel.  This
     * may require the bot to have operator status on the channel.
     * For example, if the bot has operator status, we can grant
     * operator status to "Dave" on the #cs channel
     * by calling setMode("#cs", "+o Dave");
     * An alternative way of doing this would be to use the op method.
     *
     * @param channel The channel on which to perform the mode change.
     * @param mode    The new mode to apply to the channel.  This may include
     *                zero or more arguments if necessary.
     * @see #op(String, String) op
     */
    public final void setMode(String channel, String mode) {
        this.sendRawLine("MODE " + channel + " " + mode);
    }


    /**
     * Sends an invitation to join a channel.  Some channels can be marked
     * as "invite-only", so it may be useful to allow a bot to invite people
     * into it.
     *
     * @param nick    The nick of the user to invite
     * @param channel The channel you are inviting the user to join.
     */
    public final void sendInvite(String nick, String channel) {
        this.sendRawLine("INVITE " + nick + " :" + channel);
    }


    /**
     * Bans a user from a channel.  An example of a valid hostmask is
     * "*!*compu@*.18hp.net".  This may be used in conjunction with the
     * kick method to permanently remove a user from a channel.
     * Successful use of this method may require the bot to have operator
     * status itself.
     *
     * @param channel  The channel to ban the user from.
     * @param hostmask A hostmask representing the user we're banning.
     */
    public final void ban(String channel, String hostmask) {
        this.sendRawLine("MODE " + channel + " +b " + hostmask);
    }


    /**
     * Unbans a user from a channel.  An example of a valid hostmask is
     * "*!*compu@*.18hp.net".
     * Successful use of this method may require the bot to have operator
     * status itself.
     *
     * @param channel  The channel to unban the user from.
     * @param hostmask A hostmask representing the user we're unbanning.
     */
    public final void unBan(String channel, String hostmask) {
        this.sendRawLine("MODE " + channel + " -b " + hostmask);
    }


    /**
     * Grants operator privilidges to a user on a channel.
     * Successful use of this method may require the bot to have operator
     * status itself.
     *
     * @param channel The channel we're opping the user on.
     * @param nick    The nick of the user we are opping.
     */
    public final void op(String channel, String nick) {
        this.setMode(channel, "+o " + nick);
    }


    /**
     * Removes operator privilidges from a user on a channel.
     * Successful use of this method may require the bot to have operator
     * status itself.
     *
     * @param channel The channel we're deopping the user on.
     * @param nick    The nick of the user we are deopping.
     */
    public final void deOp(String channel, String nick) {
        this.setMode(channel, "-o " + nick);
    }

    /**
     * Adds halfop priviliges from a user on a channel.
     * Successful use of this method may require the bot to have operator
     * status itself.
     *
     * @param channel The channel we're halfoping the user on.
     * @param nick    The nick of the user we are halfoping.
     */
    public final void halfOp(String channel, String nick) {
        this.setMode(channel, "+h " + nick);
    }

    /**
     * Removes halfop priviliges from a user on a channel.
     * Successful use of this method may require the bot to have operator
     * status itself.
     *
     * @param channel The channel we're dehalfoping the user on.
     * @param nick    The nick of the user we are dehalfoping.
     */
    public final void deHalfOp(String channel, String nick) {
        this.setMode(channel, "-h " + nick);
    }

    /**
     * Grants voice privilidges to a user on a channel.
     * Successful use of this method may require the bot to have operator
     * status itself.
     *
     * @param channel The channel we're voicing the user on.
     * @param nick    The nick of the user we are voicing.
     */
    public final void voice(String channel, String nick) {
        this.setMode(channel, "+v " + nick);
    }


    /**
     * Removes voice privilidges from a user on a channel.
     * Successful use of this method may require the bot to have operator
     * status itself.
     *
     * @param channel The channel we're devoicing the user on.
     * @param nick    The nick of the user we are devoicing.
     */
    public final void deVoice(String channel, String nick) {
        this.setMode(channel, "-v " + nick);
    }


    /**
     * Set the topic for a channel.
     * This method attempts to set the topic of a channel.  This
     * may require the bot to have operator status if the topic
     * is protected.
     *
     * @param channel The channel on which to perform the mode change.
     * @param topic   The new topic for the channel.
     */
    public final void setTopic(String channel, String topic) {
        this.sendRawLine("TOPIC " + channel + " :" + topic);
    }


    /**
     * Kicks a user from a channel.
     * This method attempts to kick a user from a channel and
     * may require the bot to have operator status in the channel.
     *
     * @param channel The channel to kick the user from.
     * @param nick    The nick of the user to kick.
     */
    public final void kick(String channel, String nick) {
        this.kick(channel, nick, "");
    }

    /**
     * Kicks a user from a channel, giving a reason.
     * This method attempts to kick a user from a channel and
     * may require the bot to have operator status in the channel.
     *
     * @param channel The channel to kick the user from.
     * @param nick    The nick of the user to kick.
     * @param reason  A description of the reason for kicking a user.
     */
    private void kick(String channel, String nick, String reason) {
        this.sendRawLine("KICK " + channel + " " + nick + " :" + reason);
    }

    /**
     * Performs a WHOIS on a user.
     *
     * @param nick The nick of the user to WHOIS.
     */
    public final void whois(String nick) {
        this.sendRawLineViaQueue("WHOIS " + nick);
    }

    /**
     * Quiets a user on a channel.
     *
     * @param channel The channel to QUIET the user on.
     * @param mask    The mask of the user to QUIET.
     */
    public final void quiet(String channel, String mask) {
        this.sendRawLineViaQueue("MODE " + channel + " +q " + mask);
    }


    /**
     * UnQuiets a user on a channel.
     *
     * @param channel The channel to UNQUIET the user on.
     * @param mask    The mask of the user to UNQUIET.
     */
    public final void unquiet(String channel, String mask) {
        this.sendRawLineViaQueue("MODE " + channel + " -q " + mask);
    }

    /**
     * Issues a request for a list of all channels on the IRC server.
     * When the PircBot receives information for each channel, it will
     * call the onChannelInfo method, which you will need to override
     * if you want it to do anything useful.
     *
     * @see #onChannelInfo(String, int, String) onChannelInfo
     */
    public final void listChannels() {
        this.listChannels(null);
    }


    /**
     * Issues a request for a list of all channels on the IRC server.
     * When the PircBot receives information for each channel, it will
     * call the onChannelInfo method, which you will need to override
     * if you want it to do anything useful.
     * <p/>
     * Some IRC servers support certain parameters for LIST requests.
     * One example is a parameter of ">10" to list only those channels
     * that have more than 10 users in them.  Whether these parameters
     * are supported or not will depend on the IRC server software.
     *
     * @param parameters The parameters to supply when requesting the
     *                   list.
     * @see #onChannelInfo(String, int, String) onChannelInfo
     */
    public final void listChannels(String parameters) {
        if (parameters == null) {
            this.sendRawLine("LIST");
        } else {
            this.sendRawLine("LIST " + parameters);
        }
    }


    private static String CAP_ACK_REGEX = ":(.+) CAP (.+) (LS|ACK|NAK) :(.+)";

    /**
     * This method handles events when any line of text arrives from the server,
     * then calling the appropriate method in the PircBot.  This method is
     * protected and only called by the InputThread for this instance.
     * <p/>
     * This method may not be overridden!
     *
     * @param line The raw line of text from the server.
     * @throws com.example.tom.wichatv2.Backend.Protocol.IRC.API.NickAlreadyInUseException
     * @throws IOException
     */
    protected void handleLine(String line) throws com.example.tom.wichatv2.Backend.Protocol.IRC.API.NickAlreadyInUseException, IOException {
        // Check for server pings.
        if (line.startsWith("PING ")) {
            // Respond to the ping and return immediately.
            this.onServerPing(line.substring(5));
            return;
        }


        HashMap<String, String> tags = new HashMap<>();
        Date messageDate = new Date();
        // Get tags
        if (line.startsWith("@")) {
            String[] tag_raw = line.substring(1, line.indexOf(' ')).split(";");
            for (String tag_kv : tag_raw) {
                String key = tag_kv.substring(0, tag_kv.indexOf('='));
                String valu = tag_kv.substring(tag_kv.indexOf('=') + 1);
                tags.put(key, valu);

            }
            line = line.substring(line.indexOf(' ') + 1);
        }

        if (tags.containsKey("time")) {
            // ex 2014-06-11T08:10:29.668Z
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                messageDate = sdf.parse(tags.get("time"));
            } catch (ParseException e) {
                Log.d("pIRCbot", "EX! " + e.toString());
                messageDate = new Date();
            }
        }

        String sourceNick = "";
        String sourceLogin = "";
        String sourceHostname = "";

        StringTokenizer tokenizer = new StringTokenizer(line);
        String senderInfo = tokenizer.nextToken();
        String command = tokenizer.nextToken();
        String target = null;

        int exclamation = senderInfo.indexOf("!");
        int at = senderInfo.indexOf("@");
        if (senderInfo.startsWith(":")) {
            if (exclamation > 0 && at > 0 && exclamation < at) {
                sourceNick = senderInfo.substring(1, exclamation);
                sourceLogin = senderInfo.substring(exclamation + 1, at);
                sourceHostname = senderInfo.substring(at + 1);
            } else {

                if (tokenizer.hasMoreTokens()) {

                    int code = -1;
                    try {
                        code = Integer.parseInt(command);
                    } catch (NumberFormatException e) {
                        // Keep the existing value.
                    }

                    if (code != -1) {
                        String response = line.substring(line.indexOf(command, senderInfo.length()) + 4, line.length());

                        this.processServerResponse(code, response);

                        if (code == 433 && !_registered) {
                            if (_autoNickChange) {
                                String oldNick = _nick;

                                List<String> aliases = getAliases();
                                _autoNickTries++;

                                if (_autoNickTries - 1 <= aliases.size()) {
                                    // Try next alias
                                    _nick = aliases.get(_autoNickTries - 2);
                                } else {
                                    // Append a number to the nickname
                                    _nick = getName() + (_autoNickTries - aliases.size());
                                }

                                // Notify ourself about the change
                                this.onNickChange(oldNick, getLogin(), "", _nick);

                                this.sendRawLineViaQueue("NICK " + _nick);
                            } else {
                                _socket.close();
                                _inputThread = null;
                                throw new com.example.tom.wichatv2.Backend.Protocol.IRC.API.NickAlreadyInUseException(line);
                            }
                        }

                        return;
                    } else {
                        // This is not a server response.
                        // It must be a nick without login and hostname.
                        // (or maybe a NOTICE or suchlike from the server)
                        sourceNick = senderInfo;
                        target = command;

                        // XXX: PircBot Patch - Sometimes there are senderinfos with an ident but no host
                        if (sourceNick.contains("!") && !sourceNick.contains("@")) {
                            String[] chunks = sourceNick.split("!");
                            sourceNick = chunks[0]; // Use the part before the exclamation mark
                        }

                        // XXX: PircBot Patch - (Needed for BIP IRC Proxy)
                        //      If this is a NICK command, use next token as target
                        if (command.equalsIgnoreCase("nick")) {
                            target = tokenizer.nextToken();
                        }
                    }
                } else {
                    // We don't know what this line means.
                    this.onUnknown(line);
                    // Return from the method;
                    return;
                }

            }
        }

        command = command.toUpperCase(Locale.US);
        if (sourceNick.startsWith(":")) {
            sourceNick = sourceNick.substring(1);
        }
        if (target == null) {
            target = tokenizer.nextToken();
        }
        if (target.startsWith(":")) {
            target = target.substring(1);
        }

        // Check for CTCP requests.
        if (command.equals("PRIVMSG") && line.indexOf(":\u0001") > 0 && line.endsWith("\u0001")) {
            String request = line.substring(line.indexOf(":\u0001") + 2, line.length() - 1);
            if (request.equals("VERSION")) {
                // VERSION request
                this.onCTCP(sourceNick, sourceLogin, sourceHostname, target, "VERSION");
                this.onVersion(sourceNick, sourceLogin, sourceHostname, target);
            } else if (request.startsWith("ACTION ")) {
                // ACTION request
                this.onCTCP(sourceNick, sourceLogin, sourceHostname, target, "ACTION");
                this.onAction(messageDate, sourceNick, sourceLogin, sourceHostname, target, request.substring(7));
            } else if (request.startsWith("ACTION")) {
                // This is a malformed ACTION request
                this.onMalformedCTCP(sourceNick, sourceLogin, sourceHostname, target, "ACTION");
            } else if (request.startsWith("PING ")) {
                // PING request
                this.onCTCP(sourceNick, sourceLogin, sourceHostname, target, "PING");
                this.onPing(sourceNick, sourceLogin, sourceHostname, target, request.substring(5));
            } else if (request.startsWith("PING")) {
                // This is a malformed PING request
                this.onMalformedCTCP(sourceNick, sourceLogin, sourceHostname, target, "PING");
            } else if (request.equals("TIME")) {
                // TIME request
                this.onCTCP(sourceNick, sourceLogin, sourceHostname, target, "TIME");
                this.onTime(sourceNick, sourceLogin, sourceHostname, target);
            } else if (request.equals("FINGER")) {
                // FINGER request
                this.onCTCP(sourceNick, sourceLogin, sourceHostname, target, "FINGER");
                this.onFinger(sourceNick, sourceLogin, sourceHostname, target);
            } else {
                // An unknown CTCP message - ignore it.
                this.onUnknown(line);
            }
        } else if (command.equals("PRIVMSG") && _channelPrefixes.contains(target.charAt(0))) {
            // This is a normal message to a channel.
            this.onMessage(messageDate, target, sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));
        } else if (command.equals("PRIVMSG")) {
            // This is a private message to us.
            // XXX PircBot patch to pass target info to privmsg callback
            this.onPrivateMessage(messageDate, sourceNick, sourceLogin, sourceHostname, target, line.substring(line.indexOf(" :") + 2));
        } else if (command.equals("JOIN")) {
            // Someone is joining a channel.
            this.addUser(target, this.userFactory.create("", sourceNick));
            this.onJoin(target, sourceNick, sourceLogin, sourceHostname);
        } else if (command.equals("PART")) {
            // Someone is parting from a channel.
            this.removeUser(target, sourceNick);
            if (sourceNick.equals(this.getNick())) {
                this.removeChannel(target);
            }
            this.onPart(target, sourceNick, sourceLogin, sourceHostname);
        } else if (command.equals("NICK")) {
            // Somebody is changing their nick.
            this.renameUser(sourceNick, target);
            if (sourceNick.equals(this.getNick())) {
                // Update our nick if it was us that changed nick.
                this.setNick(target);
            }
            this.onNickChange(sourceNick, sourceLogin, sourceHostname, target);
        } else if (command.equals("NOTICE")) {
            // Someone is sending a notice.
            this.onNotice(sourceNick, sourceLogin, sourceHostname, target, line.substring(line.indexOf(" :") + 2));
        } else if (command.equals("QUIT")) {
            // Someone has quit from the IRC server.

            // XXX: Pircbot Patch - Call onQuit before removing the user. This way we
            //                        are able to know which channels the user was on.
            this.onQuit(sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));

            if (sourceNick.equals(this.getNick())) {
                this.removeAllChannels();
            } else {
                this.removeUser(sourceNick);
            }
        } else if (command.equals("KICK")) {
            // Somebody has been kicked from a channel.
            String recipient = tokenizer.nextToken();
            if (recipient.equals(this.getNick())) {
                this.removeChannel(target);
            }
            this.removeUser(target, recipient);
            this.onKick(target, sourceNick, sourceLogin, sourceHostname, recipient, line.substring(line.indexOf(" :") + 2));
        } else if (command.equals("MODE")) {
            // Somebody is changing the mode on a channel or user.
            String mode = line.substring(line.indexOf(target, 2) + target.length() + 1);
            if (mode.startsWith(":")) {
                mode = mode.substring(1);
            }
            this.processMode(target, sourceNick, sourceLogin, sourceHostname, mode);
        } else if (command.equals("TOPIC")) {
            // Someone is changing the topic.
            this.onTopic(target, line.substring(line.indexOf(" :") + 2), sourceNick, System.currentTimeMillis(), true);
        } else if (command.equals("INVITE")) {
            // Somebody is inviting somebody else into a channel.
            this.onInvite(target, sourceNick, sourceLogin, sourceHostname, line.substring(line.indexOf(" :") + 2));
        } else {
            // If we reach this point, then we've found something that the PircBot
            // Doesn't currently deal with.
            this.onUnknown(line);
        }

    }


    /**
     * This method is called once the PircBot has successfully connected to
     * the IRC server.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @since PircBot 0.9.6
     */
    public void onConnect() {

    }

    /**
     * This method is called once the client is registered with the server -
     * meaning the client recieved server code 004.
     *
     * @since Yaaic
     */
    protected void onRegister() {
        _registered = true;
    }


    /**
     * This method carries out the actions to be performed when the PircBot
     * gets disconnected.  This may happen if the PircBot quits from the
     * server, or if the connection is unexpectedly lost.
     * <p/>
     * Disconnection from the IRC server is detected immediately if either
     * we or the server close the connection normally. If the connection to
     * the server is lost, but neither we nor the server have explicitly closed
     * the connection, then it may take a few minutes to detect (this is
     * commonly referred to as a "ping timeout").
     * <p/>
     * If you wish to get your IRC bot to automatically rejoin a server after
     * the connection has been lost, then this is probably the ideal method to
     * override to implement such functionality.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     */
    protected void onDisconnect() {
        _registered = false;
    }


    /**
     * This method is called by the PircBot when a numeric response
     * is received from the IRC server.  We use this method to
     * allow PircBot to process various responses from the server
     * before then passing them on to the onServerResponse method.
     * <p/>
     * Note that this method is private and should not appear in any
     * of the javadoc generated documenation.
     *
     * @param code     The three-digit numerical code for the response.
     * @param response The full response from the IRC server.
     */
    private void processServerResponse(int code, String response) {
        switch (code) {
            case RPL_WELCOME:
                // This will tell us what our real nick is.
                String real_nick = response.substring(0, response.indexOf(' '));
                if (!real_nick.equals(_nick)) {
                    // Our nick has been adjusted or changed by the server.
                    this.setNick(real_nick);
                    this.onUnknown("Your nick has been changed by the server to " + real_nick);
                }
                break;
            case RPL_LIST: {
                // This is a bit of information about a channel.
                int firstSpace = response.indexOf(' ');
                int secondSpace = response.indexOf(' ', firstSpace + 1);
                int thirdSpace = response.indexOf(' ', secondSpace + 1);
                int colon = response.indexOf(':');
                String channel = response.substring(firstSpace + 1, secondSpace);
                int userCount = 0;
                try {
                    userCount = Integer.parseInt(response.substring(secondSpace + 1, thirdSpace));
                } catch (NumberFormatException e) {
                    // Stick with the value of zero.
                }
                String topic = response.substring(colon + 1);
                this.onChannelInfo(channel, userCount, topic);
                break;
            }
            case RPL_TOPIC: {
                // This is topic information about a channel we've just joined.
                int firstSpace = response.indexOf(' ');
                int secondSpace = response.indexOf(' ', firstSpace + 1);
                int colon = response.indexOf(':');
                String channel = response.substring(firstSpace + 1, secondSpace);
                String topic = response.substring(colon + 1);

                _topics.put(channel, topic);

                // For backwards compatibility only - this onTopic method is deprecated.
                this.onTopic(channel, topic);
                break;
            }
            case RPL_TOPICINFO: {
                StringTokenizer tokenizer = new StringTokenizer(response);
                tokenizer.nextToken();
                String channel = tokenizer.nextToken();
                String setBy = tokenizer.nextToken();
                long date = 0;
                try {
                    date = Long.parseLong(tokenizer.nextToken()) * 1000;
                } catch (NumberFormatException e) {
                    // Stick with the default value of zero.
                }

                String topic = _topics.get(channel);
                _topics.remove(channel);

                this.onTopic(channel, topic, setBy, date, false);
                break;
            }
            case RPL_NAMREPLY: {
                // This is a list of nicks in a channel that we've just joined.
                int channelEndIndex = response.indexOf(" :");
                String channel = response.substring(response.lastIndexOf(' ', channelEndIndex - 1) + 1, channelEndIndex);

                StringTokenizer tokenizer = new StringTokenizer(response.substring(response.indexOf(" :") + 2));
                while (tokenizer.hasMoreTokens()) {
                    String nick = tokenizer.nextToken();
                    String prefix = "";
                    // Some IRC servers send just the nick, others send the format <Nick>!<username>@<host>
                    // This is unfortunately, not part of the original IRC standard, which defines the pattern to be
                    // [=*@](channel)\ \:[@+](nick)(\ [@+](nick))+?
                    // Somehow, ZNC started doing odd things. I fixed it.
                    // I heavily suspect that this is caused by multi-prefix?
                    if (nick.contains("!")) {
                        nick = nick.substring(0, nick.indexOf("!"));
                    }
                    this.addUser(channel, this.userFactory.create(prefix, nick));
                }
                break;
            }
            case RPL_ENDOFNAMES: {
                // This is the end of a NAMES list, so we know that we've got
                // the full list of users in the channel that we just joined.
                String channel = response.substring(response.indexOf(' ') + 1, response.indexOf(" :"));
                User[] users = this.getUsers(channel);
                this.onUserList(channel, users);
                break;
            }
        }

        this.onServerResponse(code, response);
    }


    /**
     * This method is called when we receive a numeric response from the
     * IRC server.
     * <p/>
     * Numerics in the range from 001 to 099 are used for client-server
     * connections only and should never travel between servers.  Replies
     * generated in response to commands are found in the range from 200
     * to 399.  Error replies are found in the range from 400 to 599.
     * <p/>
     * For example, we can use this method to discover the topic of a
     * channel when we join it.  If we join the channel #test which
     * has a topic of &quot;I am King of Test&quot; then the response
     * will be &quot;<code>PircBot #test :I Am King of Test</code>&quot;
     * with a code of 332 to signify that this is a topic.
     * (This is just an example - note that overriding the
     * <code>onTopic</code> method is an easier way of finding the
     * topic for a channel). Check the IRC RFC for the full list of other
     * command response codes.
     * <p/>
     * PircBot implements the interface ReplyConstants, which contains
     * contstants that you may find useful here.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param code     The three-digit numerical code for the response.
     * @param response The full response from the IRC server.
     * @see com.example.tom.wichatv2.Backend.Protocol.IRC.API.ReplyConstants
     */
    protected void onServerResponse(int code, String response) {
    }

    /**
     * This method is called when there is a generic message about the
     * that may be interesting for the user to see.
     * <p/>
     * This is 100% generated here in the connection.
     *
     * @param message
     */
    protected void onConnectionMessage(String message) {
    }

    /**
     * This method is called when we receive a user list from the server
     * after joining a channel.
     * <p/>
     * Shortly after joining a channel, the IRC server sends a list of all
     * users in that channel. The PircBot collects this information and
     * calls this method as soon as it has the full list.
     * <p/>
     * To obtain the nick of each user in the channel, call the getNick()
     * method on each User object in the array.
     * <p/>
     * At a later time, you may call the getUsers method to obtain an
     * up to date list of the users in the channel.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The name of the channel.
     * @param users   An array of User objects belonging to this channel.
     * @see User
     * @since PircBot 1.0.0
     */
    protected void onUserList(String channel, User[] users) {
    }


    /**
     * This method is called whenever a message is sent to a channel.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel  The channel to which the message was sent.
     * @param sender   The nick of the person who sent the message.
     * @param login    The login of the person who sent the message.
     * @param hostname The hostname of the person who sent the message.
     * @param message  The actual message sent to the channel.
     */
    protected void onMessage(Date evDate, String channel, String sender, String login, String hostname, String message) {
    }


    /**
     * This method is called whenever a private message is sent to the PircBot.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param sender   The nick of the person who sent the private message.
     * @param login    The login of the person who sent the private message.
     * @param hostname The hostname of the person who sent the private message.
     * @param message  The actual message.
     */
    // XXX PircBot patch to pass target info to privmsg callback
    protected void onPrivateMessage(Date evDate, String sender, String login, String hostname, String target, String message) {
    }


    /**
     * This method is called whenever an ACTION is sent from a user.  E.g.
     * such events generated by typing "/me goes shopping" in most IRC clients.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param messageDate Date when the action happened.
     * @param sender      The nick of the user that sent the action.
     * @param login       The login of the user that sent the action.
     * @param hostname    The hostname of the user that sent the action.
     * @param target      The target of the action, be it a channel or our nick.
     * @param action      The action carried out by the user.
     */
    protected void onAction(Date messageDate, String sender, String login, String hostname, String target, String action) {
    }


    /**
     * This method is called whenever we receive a notice.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param sourceNick     The nick of the user that sent the notice.
     * @param sourceLogin    The login of the user that sent the notice.
     * @param sourceHostname The hostname of the user that sent the notice.
     * @param target         The target of the notice, be it our nick or a channel name.
     * @param notice         The notice message.
     */
    protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
    }


    /**
     * This method is called whenever someone (possibly us) joins a channel
     * which we are on.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel  The channel which somebody joined.
     * @param sender   The nick of the user who joined the channel.
     * @param login    The login of the user who joined the channel.
     * @param hostname The hostname of the user who joined the channel.
     */
    protected void onJoin(String channel, String sender, String login, String hostname) {
    }


    /**
     * This method is called whenever someone (possibly us) parts a channel
     * which we are on.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel  The channel which somebody parted from.
     * @param sender   The nick of the user who parted from the channel.
     * @param login    The login of the user who parted from the channel.
     * @param hostname The hostname of the user who parted from the channel.
     */
    protected void onPart(String channel, String sender, String login, String hostname) {
    }


    /**
     * This method is called whenever someone (possibly us) changes nick on any
     * of the channels that we are on.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param oldNick  The old nick.
     * @param login    The login of the user.
     * @param hostname The hostname of the user.
     * @param newNick  The new nick.
     */
    protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
    }


    /**
     * This method is called whenever someone (possibly us) is kicked from
     * any of the channels that we are in.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel from which the recipient was kicked.
     * @param kickerNick     The nick of the user who performed the kick.
     * @param kickerLogin    The login of the user who performed the kick.
     * @param kickerHostname The hostname of the user who performed the kick.
     * @param recipientNick  The unfortunate recipient of the kick.
     * @param reason         The reason given by the user who performed the kick.
     */
    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
    }


    /**
     * This method is called whenever someone (possibly us) quits from the
     * server.  We will only observe this if the user was in one of the
     * channels to which we are connected.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param sourceNick     The nick of the user that quit from the server.
     * @param sourceLogin    The login of the user that quit from the server.
     * @param sourceHostname The hostname of the user that quit from the server.
     * @param reason         The reason given for quitting the server.
     */
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
    }


    /**
     * This method is called whenever a user sets the topic, or when
     * PircBot joins a new channel and discovers its topic.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The channel that the topic belongs to.
     * @param topic   The topic for the channel.
     * @deprecated As of 1.2.0, replaced by {@link #onTopic(String, String, String, long, boolean)}
     */
    @Deprecated
    protected void onTopic(String channel, String topic) {
    }


    /**
     * This method is called whenever a user sets the topic, or when
     * PircBot joins a new channel and discovers its topic.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel The channel that the topic belongs to.
     * @param topic   The topic for the channel.
     * @param setBy   The nick of the user that set the topic.
     * @param date    When the topic was set (milliseconds since the epoch).
     * @param changed True if the topic has just been changed, false if
     *                the topic was already there.
     */
    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
    }


    /**
     * After calling the listChannels() method in PircBot, the server
     * will start to send us information about each channel on the
     * server.  You may override this method in order to receive the
     * information about each channel as soon as it is received.
     * <p/>
     * Note that certain channels, such as those marked as hidden,
     * may not appear in channel listings.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel   The name of the channel.
     * @param userCount The number of users visible in this channel.
     * @param topic     The topic for this channel.
     * @see #listChannels() listChannels
     */
    protected void onChannelInfo(String channel, int userCount, String topic) {
    }


    /**
     * Called when the mode of a channel is set.  We process this in
     * order to call the appropriate onOp, onDeop, etc method before
     * finally calling the override-able onMode method.
     * <p/>
     * Note that this method is private and is not intended to appear
     * in the javadoc generated documentation.
     *
     * @param target         The channel or nick that the mode operation applies to.
     * @param sourceNick     The nick of the user that set the mode.
     * @param sourceLogin    The login of the user that set the mode.
     * @param sourceHostname The hostname of the user that set the mode.
     * @param mode           The mode that has been set.
     */
    private void processMode(String target, String sourceNick, String sourceLogin, String sourceHostname, String mode) {

        if (_channelPrefixes.indexOf(target.charAt(0)) >= 0) {
            // The mode of a channel is being changed.
            StringTokenizer tok = new StringTokenizer(mode);
            String[] params = new String[tok.countTokens()];

            int t = 0;
            while (tok.hasMoreTokens()) {
                params[t] = tok.nextToken();
                t++;
            }

            char pn = ' ';
            int p = 1;

            // All of this is very large and ugly, but it's the only way of providing
            // what the users want :-/
            for (int i = 0; i < params[0].length(); i++) {
                char atPos = params[0].charAt(i);

                switch (atPos) {
                    case '+':
                    case '-':
                        pn = atPos;
                        break;
                    case 'o':
                        if (pn == '+') {
                            this.updateUser(target, OP_ADD, params[p]);
                            onOp(target, sourceNick, sourceLogin, sourceHostname, params[p]);
                        } else {
                            this.updateUser(target, OP_REMOVE, params[p]);
                            onDeop(target, sourceNick, sourceLogin, sourceHostname, params[p]);
                        }
                        p++;
                        break;
                    case 'h':
                        if (pn == '+') {
                            this.updateUser(target, HALFOP_ADD, params[p]);
                            onHalfop(target, sourceNick, sourceLogin, sourceHostname, params[p]);
                        } else {
                            this.updateUser(target, HALFOP_REMOVE, params[p]);
                            onDeHalfop(target, sourceNick, sourceLogin, sourceHostname, params[p]);
                        }
                        p++;
                        break;
                    case 'v':
                        if (pn == '+') {
                            this.updateUser(target, VOICE_ADD, params[p]);
                            onVoice(target, sourceNick, sourceLogin, sourceHostname, params[p]);
                        } else {
                            this.updateUser(target, VOICE_REMOVE, params[p]);
                            onDeVoice(target, sourceNick, sourceLogin, sourceHostname, params[p]);
                        }
                        p++;
                        break;
                    case 'k':
                        if (pn == '+') {
                            onSetChannelKey(target, sourceNick, sourceLogin, sourceHostname, params[p]);
                        } else {
                            onRemoveChannelKey(target, sourceNick, sourceLogin, sourceHostname, params[p]);
                        }
                        p++;
                        break;
                    case 'l':
                        if (pn == '+') {
                            onSetChannelLimit(target, sourceNick, sourceLogin, sourceHostname, Integer.parseInt(params[p]));
                            p++;
                        } else {
                            onRemoveChannelLimit(target, sourceNick, sourceLogin, sourceHostname);
                        }
                        break;
                    case 'b':
                        if (pn == '+') {
                            onSetChannelBan(target, sourceNick, sourceLogin, sourceHostname, params[p]);
                        } else {
                            onRemoveChannelBan(target, sourceNick, sourceLogin, sourceHostname, params[p]);
                        }
                        p++;
                        break;
                    case 't':
                        if (pn == '+') {
                            onSetTopicProtection(target, sourceNick, sourceLogin, sourceHostname);
                        } else {
                            onRemoveTopicProtection(target, sourceNick, sourceLogin, sourceHostname);
                        }
                        break;
                    case 'n':
                        if (pn == '+') {
                            onSetNoExternalMessages(target, sourceNick, sourceLogin, sourceHostname);
                        } else {
                            onRemoveNoExternalMessages(target, sourceNick, sourceLogin, sourceHostname);
                        }
                        break;
                    case 'i':
                        if (pn == '+') {
                            onSetInviteOnly(target, sourceNick, sourceLogin, sourceHostname);
                        } else {
                            onRemoveInviteOnly(target, sourceNick, sourceLogin, sourceHostname);
                        }
                        break;
                    case 'm':
                        if (pn == '+') {
                            onSetModerated(target, sourceNick, sourceLogin, sourceHostname);
                        } else {
                            onRemoveModerated(target, sourceNick, sourceLogin, sourceHostname);
                        }
                        break;
                    case 'p':
                        if (pn == '+') {
                            onSetPrivate(target, sourceNick, sourceLogin, sourceHostname);
                        } else {
                            onRemovePrivate(target, sourceNick, sourceLogin, sourceHostname);
                        }
                        break;
                    case 's':
                        if (pn == '+') {
                            onSetSecret(target, sourceNick, sourceLogin, sourceHostname);
                        } else {
                            onRemoveSecret(target, sourceNick, sourceLogin, sourceHostname);
                        }
                        break;
                }
            }

            this.onMode(target, sourceNick, sourceLogin, sourceHostname, mode);
        } else {
            // The mode of a user is being changed.
            this.onUserMode(target, sourceNick, sourceLogin, sourceHostname, mode);
        }
    }


    /**
     * Called when the mode of a channel is set.
     * <p/>
     * You may find it more convenient to decode the meaning of the mode
     * string by overriding the onOp, onDeOp, onVoice, onDeVoice,
     * onChannelKey, onDeChannelKey, onChannelLimit, onDeChannelLimit,
     * onChannelBan or onDeChannelBan methods as appropriate.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel that the mode operation applies to.
     * @param sourceNick     The nick of the user that set the mode.
     * @param sourceLogin    The login of the user that set the mode.
     * @param sourceHostname The hostname of the user that set the mode.
     * @param mode           The mode that has been set.
     */
    protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
    }

    /**
     * Called when the mode of a user is set.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param targetNick     The nick that the mode operation applies to.
     * @param sourceNick     The nick of the user that set the mode.
     * @param sourceLogin    The login of the user that set the mode.
     * @param sourceHostname The hostname of the user that set the mode.
     * @param mode           The mode that has been set.
     * @since PircBot 1.2.0
     */
    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
    }


    /**
     * Called when a user (possibly us) gets granted operator status for a channel.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param recipient      The nick of the user that got 'opped'.
     * @since PircBot 0.9.5
     */
    protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
    }


    /**
     * Called when a user (possibly us) gets operator status taken away.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param recipient      The nick of the user that got 'deopped'.
     * @since PircBot 0.9.5
     */
    protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
    }

    /**
     * Called when a user (possibly us) gets halfoperator status added.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param recipient      The nick of the user that got 'halfoped'.
     * @since PircBot 0.9.5
     */
    protected void onHalfop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
    }

    /**
     * Called when a user (possibly us) gets halfoperator status added.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param recipient      The nick of the user that got 'dehalfoped'.
     * @since PircBot 0.9.5
     */
    protected void onDeHalfop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
    }


    /**
     * Called when a user (possibly us) gets voice status granted in a channel.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param recipient      The nick of the user that got 'voiced'.
     * @since PircBot 0.9.5
     */
    protected void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
    }


    /**
     * Called when a user (possibly us) gets voice status removed.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param recipient      The nick of the user that got 'devoiced'.
     * @since PircBot 0.9.5
     */
    protected void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
    }


    /**
     * Called when a channel key is set.  When the channel key has been set,
     * other users may only join that channel if they know the key.  Channel keys
     * are sometimes referred to as passwords.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param key            The new key for the channel.
     * @since PircBot 0.9.5
     */
    protected void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
    }


    /**
     * Called when a channel key is removed.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param key            The key that was in use before the channel key was removed.
     * @since PircBot 0.9.5
     */
    protected void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
    }


    /**
     * Called when a user limit is set for a channel.  The number of users in
     * the channel cannot exceed this limit.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param limit          The maximum number of users that may be in this channel at the same time.
     * @since PircBot 0.9.5
     */
    protected void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {
    }


    /**
     * Called when the user limit is removed for a channel.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }


    /**
     * Called when a user (possibly us) gets banned from a channel.  Being
     * banned from a channel prevents any user with a matching hostmask from
     * joining the channel.  For this reason, most bans are usually directly
     * followed by the user being kicked :-)
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param hostmask       The hostmask of the user that has been banned.
     * @since PircBot 0.9.5
     */
    protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
    }


    /**
     * Called when a hostmask ban is removed from a channel.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @param hostmask
     * @since PircBot 0.9.5
     */
    protected void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
    }


    /**
     * Called when topic protection is enabled for a channel.  Topic protection
     * means that only operators in a channel may change the topic.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }


    /**
     * Called when topic protection is removed for a channel.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }


    /**
     * Called when a channel is set to only allow messages from users that
     * are in the channel.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }


    /**
     * Called when a channel is set to allow messages from any user, even
     * if they are not actually in the channel.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }


    /**
     * Called when a channel is set to 'invite only' mode.  A user may only
     * join the channel if they are invited by someone who is already in the
     * channel.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }


    /**
     * Called when a channel has 'invite only' removed.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }


    /**
     * Called when a channel is set to 'moderated' mode.  If a channel is
     * moderated, then only users who have been 'voiced' or 'opped' may speak
     * or change their nicks.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }


    /**
     * Called when a channel has moderated mode removed.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }


    /**
     * Called when a channel is marked as being in private mode.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }


    /**
     * Called when a channel is marked as not being in private mode.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }


    /**
     * Called when a channel is set to be in 'secret' mode.  Such channels
     * typically do not appear on a server's channel listing.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }

    /**
     * Called when a channel has 'secret' mode removed.
     * <p/>
     * This is a type of mode change and is also passed to the onMode
     * method in the PircBot class.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param channel        The channel in which the mode change took place.
     * @param sourceNick     The nick of the user that performed the mode change.
     * @param sourceLogin    The login of the user that performed the mode change.
     * @param sourceHostname The hostname of the user that performed the mode change.
     * @since PircBot 0.9.5
     */
    protected void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
    }

    /**
     * Called when we are invited to a channel by a user.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param targetNick     The nick of the user being invited - should be us!
     * @param sourceNick     The nick of the user that sent the invitation.
     * @param sourceLogin    The login of the user that sent the invitation.
     * @param sourceHostname The hostname of the user that sent the invitation.
     * @param channel        The channel that we're being invited to.
     * @since PircBot 0.9.5
     */
    protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
    }


    /**
     * This method is called whenever we receive a VERSION request.
     * This abstract implementation responds with the PircBot's _version string,
     * so if you override this method, be sure to either mimic its functionality
     * or to call super.onVersion(...);
     *
     * @param sourceNick     The nick of the user that sent the VERSION request.
     * @param sourceLogin    The login of the user that sent the VERSION request.
     * @param sourceHostname The hostname of the user that sent the VERSION request.
     * @param target         The target of the VERSION request, be it our nick or a channel name.
     */
    protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001VERSION " + _version + "\u0001");
    }


    /**
     * This method is called whenever we receive a PING request from another
     * user.
     * <p/>
     * This abstract implementation responds correctly, so if you override this
     * method, be sure to either mimic its functionality or to call
     * super.onPing(...);
     *
     * @param sourceNick     The nick of the user that sent the PING request.
     * @param sourceLogin    The login of the user that sent the PING request.
     * @param sourceHostname The hostname of the user that sent the PING request.
     * @param target         The target of the PING request, be it our nick or a channel name.
     * @param pingValue      The value that was supplied as an argument to the PING command.
     */
    protected void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001PING " + pingValue + "\u0001");
    }


    /**
     * The actions to perform when a PING request comes from the server.
     * <p/>
     * This sends back a correct response, so if you override this method,
     * be sure to either mimic its functionality or to call
     * super.onServerPing(response);
     *
     * @param response The response that should be given back in your PONG.
     */
    protected void onServerPing(String response) {
        this.sendRawLine("PONG " + response);
        if (!firstPing) {
            this.joinChannel("#" + this.chatroom.getName());
            firstPing = true;
        }
    }


    /**
     * This method is called whenever we receive a TIME request.
     * <p/>
     * This abstract implementation responds correctly, so if you override this
     * method, be sure to either mimic its functionality or to call
     * super.onTime(...);
     *
     * @param sourceNick     The nick of the user that sent the TIME request.
     * @param sourceLogin    The login of the user that sent the TIME request.
     * @param sourceHostname The hostname of the user that sent the TIME request.
     * @param target         The target of the TIME request, be it our nick or a channel name.
     */
    protected void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001TIME " + new Date().toString() + "\u0001");
    }


    /**
     * This method is called whenever we receive a FINGER request.
     * <p/>
     * This abstract implementation responds correctly, so if you override this
     * method, be sure to either mimic its functionality or to call
     * super.onFinger(...);
     *
     * @param sourceNick     The nick of the user that sent the FINGER request.
     * @param sourceLogin    The login of the user that sent the FINGER request.
     * @param sourceHostname The hostname of the user that sent the FINGER request.
     * @param target         The target of the FINGER request, be it our nick or a channel name.
     */
    protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        this.sendRawLine("NOTICE " + sourceNick + " :\u0001FINGER " + _finger + "\u0001");
    }


    /**
     * This method is called whenever we receive a line from the server that
     * the PircBot has not been programmed to recognise.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param line The raw line that was received from the server.
     */
    protected void onUnknown(String line) {
        // And then there were none :)
    }

    /**
     * This method is called whenever we receive a CTCP command that
     * the PircBot has not been programmed to recognise.
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param sourceNick     The nick of the user that sent the malformed CTCP request.
     * @param sourceLogin    The login of the user that sent the malformed CTCP request.
     * @param sourceHostname The hostname of the user that sent the malformed CTCP request.
     * @param target         The target of the malformed CTCP request, be it our nick or a channel name.
     * @param command        The name of the malformed CTCP request
     */
    protected void onMalformedCTCP(String sourceNick, String sourceLogin, String sourceHostname, String target, String command) {
        // This does nothing :)
    }

    /**
     * This method is called whenever we receive a CTCP command
     * <p/>
     * The implementation of this method in the PircBot abstract class
     * performs no actions and may be overridden as required.
     *
     * @param sourceNick     The nick of the user that sent the malformed CTCP request.
     * @param sourceLogin    The login of the user that sent the malformed CTCP request.
     * @param sourceHostname The hostname of the user that sent the malformed CTCP request.
     * @param target         The target of the malformed CTCP request, be it our nick or a channel name.
     * @param command        The name of the malformed CTCP request
     */
    protected void onCTCP(String sourceNick, String sourceLogin, String sourceHostname, String target, String command) {
        // This does nothing :)
    }

    /**
     * Sets the name of the bot, which will be used as its nick when it
     * tries to join an IRC server.  This should be set before joining
     * any servers, otherwise the default nick will be used.  You would
     * typically call this method from the constructor of the class that
     * extends PircBot.
     * <p/>
     * The changeNick method should be used if you wish to change your nick
     * when you are connected to a server.
     *
     * @param name The new name of the Bot.
     */
    protected final void setName(String name) {
        _name = name;
        _nick = name;
    }

    public final void setAliases(Collection<String> aliases) {
        _aliases.clear();
        _aliases.addAll(aliases);
    }

    /**
     * Sets the internal nick of the bot.  This is only to be called by the
     * PircBot class in response to notification of nick changes that apply
     * to us.
     *
     * @param nick The new nick.
     */
    private void setNick(String nick) {
        _nick = nick;
    }


    /**
     * Sets the internal login of the Bot.  This should be set before joining
     * any servers.
     *
     * @param login The new login of the Bot.
     */
    protected final void setLogin(String login) {
        _login = login;
    }


    /**
     * Sets the internal version of the Bot.  This should be set before joining
     * any servers.
     *
     * @param version The new version of the Bot.
     */
    protected final void setVersion(String version) {
        _version = version;
    }


    /**
     * Sets the interal finger message.  This should be set before joining
     * any servers.
     *
     * @param finger The new finger message for the Bot.
     */
    protected final void setFinger(String finger) {
        _finger = finger;
    }


    /**
     * Gets the name of the PircBot. This is the name that will be used as
     * as a nick when we try to join servers.
     *
     * @return The name of the PircBot.
     */
    public final String getName() {
        return _name;
    }

    public final List<String> getAliases() {
        return Collections.unmodifiableList(_aliases);
    }

    /**
     * Returns the current nick of the bot. Note that if you have just changed
     * your nick, this method will still return the old nick until confirmation
     * of the nick change is received from the server.
     * <p/>
     * The nick returned by this method is maintained only by the PircBot
     * class and is guaranteed to be correct in the context of the IRC server.
     *
     * @return The current nick of the bot.
     * @since PircBot 1.0.0
     */
    public String getNick() {
        return _nick;
    }


    /**
     * Gets the internal login of the PircBot.
     *
     * @return The login of the PircBot.
     */
    public final String getLogin() {
        return _login;
    }


    /**
     * Gets the internal version of the PircBot.
     *
     * @return The version of the PircBot.
     */
    public final String getVersion() {
        return _version;
    }


    /**
     * Gets the internal finger message of the PircBot.
     *
     * @return The finger message of the PircBot.
     */
    public final String getFinger() {
        return _finger;
    }


    /**
     * Returns whether or not the PircBot is currently connected to a server.
     * The result of this method should only act as a rough guide,
     * as the result may not be valid by the time you act upon it.
     *
     * @return True if and only if the PircBot is currently connected to a server.
     */
    public final synchronized boolean isConnected() {
        return _inputThread != null && _inputThread.isConnected();
    }

    /**
     * Returns wether or not the client is registered with the server.
     *
     * @return True if server code 004 has been received. False otherwise.
     * @since Yaaic
     */
    public final synchronized boolean isRegistered() {
        return _registered;
    }


    /**
     * Sets the number of milliseconds to delay between consecutive
     * messages when there are multiple messages waiting in the
     * outgoing message queue.  This has a default value of 1000ms.
     * It is a good idea to stick to this default value, as it will
     * prevent your bot from spamming servers and facing the subsequent
     * wrath!  However, if you do need to change this delay value (<b>not
     * recommended</b>), then this is the method to use.
     *
     * @param delay The number of milliseconds between each outgoing message.
     */
    public final void setMessageDelay(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("Cannot have a negative time.");
        }
        _messageDelay = delay;
    }


    /**
     * Returns the number of milliseconds that will be used to separate
     * consecutive messages to the server from the outgoing message queue.
     *
     * @return Number of milliseconds.
     */
    public final long getMessageDelay() {
        return _messageDelay;
    }


    /**
     * Gets the maximum length of any line that is sent via the IRC protocol.
     * The IRC RFC specifies that line lengths, including the trailing \r\n
     * must not exceed 512 bytes.  Hence, there is currently no option to
     * change this value in PircBot.  All lines greater than this length
     * will be truncated before being sent to the IRC server.
     *
     * @return The maximum line length (currently fixed at 512)
     */
    public final int getMaxLineLength() {
        return com.example.tom.wichatv2.Backend.Protocol.IRC.API.InputThread.MAX_LINE_LENGTH;
    }


    /**
     * Gets the number of lines currently waiting in the outgoing message Queue.
     * If this returns 0, then the Queue is empty and any new message is likely
     * to be sent to the IRC server immediately.
     *
     * @return The number of lines in the outgoing message Queue.
     * @since PircBot 0.9.9
     */
    public final int getOutgoingQueueSize() {
        return _outQueue.size();
    }


    /**
     * Returns the name of the last IRC server the PircBot tried to connect to.
     * This does not imply that the connection attempt to the server was
     * successful (we suggest you look at the onConnect method).
     * A value of null is returned if the PircBot has never tried to connect
     * to a server.
     *
     * @return The name of the last machine we tried to connect to. Returns
     * null if no connection attempts have ever been made.
     */
    public final String getServer() {
        return _server;
    }


    /**
     * Returns the port number of the last IRC server that the PircBot tried
     * to connect to.
     * This does not imply that the connection attempt to the server was
     * successful (we suggest you look at the onConnect method).
     * A value of -1 is returned if the PircBot has never tried to connect
     * to a server.
     *
     * @return The port number of the last IRC server we connected to.
     * Returns -1 if no connection attempts have ever been made.
     * @since PircBot 0.9.9
     */
    public final int getPort() {
        return _port;
    }


    /**
     * Returns the last password that we used when connecting to an IRC server.
     * This does not imply that the connection attempt to the server was
     * successful (we suggest you look at the onConnect method).
     * A value of null is returned if the PircBot has never tried to connect
     * to a server using a password.
     *
     * @return The last password that we used when connecting to an IRC server.
     * Returns null if we have not previously connected using a password.
     * @since PircBot 0.9.9
     */
    public final String getPassword() {
        return _password;
    }


    /**
     * A convenient method that accepts an IP address represented as a
     * long and returns an integer array of size 4 representing the same
     * IP address.
     *
     * @param address the long value representing the IP address.
     * @return An int[] of size 4.
     * @since PircBot 0.9.4
     */
    public int[] longToIp(long address) {
        int[] ip = new int[4];
        for (int i = 3; i >= 0; i--) {
            ip[i] = (int) (address % 256);
            address = address / 256;
        }
        return ip;
    }


    /**
     * A convenient method that accepts an IP address represented by a byte[]
     * of size 4 and returns this as a long representation of the same IP
     * address.
     *
     * @param address the byte[] of size 4 representing the IP address.
     * @return a long representation of the IP address.
     * @since PircBot 0.9.4
     */
    public long ipToLong(byte[] address) {
        if (address.length != 4) {
            throw new IllegalArgumentException("byte array must be of length 4");
        }
        long ipNum = 0;
        long multiplier = 1;
        for (int i = 3; i >= 0; i--) {
            int byteVal = (address[i] + 256) % 256;
            ipNum += byteVal * multiplier;
            multiplier *= 256;
        }
        return ipNum;
    }


    /**
     * Sets the encoding charset to be used when sending or receiving lines
     * from the IRC server.  If set to null, then the platform's default
     * charset is used.  You should only use this method if you are
     * trying to send text to an IRC server in a different charset, e.g.
     * "GB2312" for Chinese encoding.  If a PircBot is currently connected
     * to a server, then it must reconnect before this change takes effect.
     *
     * @param charset The new encoding charset to be used by PircBot.
     * @throws UnsupportedEncodingException If the named charset is not
     *                                      supported.
     * @since PircBot 1.0.4
     */
    public void setEncoding(String charset) throws UnsupportedEncodingException {
        // Just try to see if the charset is supported first...
        "".getBytes(charset);

        _charset = charset;
    }


    /**
     * Returns the encoding used to send and receive lines from
     * the IRC server, or null if not set.  Use the setEncoding
     * method to change the encoding charset.
     *
     * @return The encoding used to send outgoing messages, or
     * null if not set.
     * @since PircBot 1.0.4
     */
    public String getEncoding() {
        return _charset;
    }

    /**
     * Returns the InetAddress used by the PircBot.
     * This can be used to find the I.P. address from which the PircBot is
     * connected to a server.
     *
     * @return The current local InetAddress, or null if never connected.
     * @since PircBot 1.4.4
     */
    public InetAddress getInetAddress() {
        return _inetAddress;
    }


    /**
     * Sets the InetAddress to be used when sending DCC chat or file transfers.
     * This can be very useful when you are running a bot on a machine which
     * is behind a firewall and you need to tell receiving clients to connect
     * to a NAT/router, which then forwards the connection.
     *
     * @param dccInetAddress The new InetAddress, or null to use the default.
     * @since PircBot 1.4.4
     */
    public void setDccInetAddress(InetAddress dccInetAddress) {
        _dccInetAddress = dccInetAddress;
    }


    /**
     * Returns the InetAddress used when sending DCC chat or file transfers.
     * If this is null, the default InetAddress will be used.
     *
     * @return The current DCC InetAddress, or null if left as default.
     * @since PircBot 1.4.4
     */
    public InetAddress getDccInetAddress() {
        return _dccInetAddress;
    }


    /**
     * Returns the set of port numbers to be used when sending a DCC chat
     * or file transfer. This is useful when you are behind a firewall and
     * need to set up port forwarding. The array of port numbers is traversed
     * in sequence until a free port is found to listen on. A DCC tranfer will
     * fail if all ports are already in use.
     * If set to null, <i>any</i> free port number will be used.
     *
     * @return An array of port numbers that PircBot can use to send DCC
     * transfers, or null if any port is allowed.
     * @since PircBot 1.4.4
     */
    public int[] getDccPorts() {
        if (_dccPorts == null || _dccPorts.length == 0) {
            return null;
        }
        // Clone the array to prevent external modification.
        return _dccPorts.clone();
    }


    /**
     * Sets the choice of port numbers that can be used when sending a DCC chat
     * or file transfer. This is useful when you are behind a firewall and
     * need to set up port forwarding. The array of port numbers is traversed
     * in sequence until a free port is found to listen on. A DCC tranfer will
     * fail if all ports are already in use.
     * If set to null, <i>any</i> free port number will be used.
     *
     * @param ports The set of port numbers that PircBot may use for DCC
     *              transfers, or null to let it use any free port (default).
     * @since PircBot 1.4.4
     */
    public void setDccPorts(int[] ports) {
        if (ports == null || ports.length == 0) {
            _dccPorts = null;
        } else {
            // Clone the array to prevent external modification.
            _dccPorts = ports.clone();
        }
    }


    /**
     * Returns true if and only if the object being compared is the exact
     * same instance as this PircBot. This may be useful if you are writing
     * a multiple server IRC bot that uses more than one instance of PircBot.
     *
     * @return true if and only if Object o is a PircBot and equal to this.
     * @since PircBot 0.9.9
     */
    @Override
    public boolean equals(Object o) {
        // This probably has the same effect as Object.equals, but that may change...
        if (o instanceof IRCClient) {
            IRCClient other = (IRCClient) o;
            return other == this;
        }
        return false;
    }


    /**
     * Returns the hashCode of this PircBot. This method can be called by hashed
     * collection classes and is useful for managing multiple instances of
     * PircBots in such collections.
     *
     * @return the hash code for this instance of PircBot.
     * @since PircBot 0.9.9
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }


    /**
     * Returns a String representation of this object.
     * You may find this useful for debugging purposes, particularly
     * if you are using more than one PircBot instance to achieve
     * multiple server connectivity. The format of
     * this String may change between different versions of PircBot
     * but is currently something of the form
     * <code>
     * Version{PircBot x.y.z Java IRC Bot - www.jibble.org}
     * Connected{true}
     * Server{irc.dal.net}
     * Port{6667}
     * Password{}
     * </code>
     *
     * @return a String representation of this object.
     * @since PircBot 0.9.10
     */
    @Override
    public String toString() {
        return "Version{" + _version + "}" +
                " Connected{" + isConnected() + "}" +
                " Server{" + _server + "}" +
                " Port{" + _port + "}" +
                " Password{" + _password + "}";
    }


    /**
     * Returns an array of all users in the specified channel.
     * <p/>
     * There are some important things to note about this method:-
     * <ul>
     * <li>This method may not return a full list of users if you call it
     * before the complete nick list has arrived from the IRC server.
     * </li>
     * <li>If you wish to find out which users are in a channel as soon
     * as you join it, then you should override the onUserList method
     * instead of calling this method, as the onUserList method is only
     * called as soon as the full user list has been received.
     * </li>
     * <li>This method will return immediately, as it does not require any
     * interaction with the IRC server.
     * </li>
     * <li>The bot must be in a channel to be able to know which users are
     * in it.
     * </li>
     * </ul>
     *
     * @param channel The name of the channel to list.
     * @return An array of User objects. This array is empty if we are not
     * in the channel.
     * @see #onUserList(String, User[]) onUserList
     * @since PircBot 1.0.0
     */
    public final User[] getUsers(String channel) {
        channel = channel.toLowerCase(Locale.US);
        User[] userArray = new User[0];
        synchronized (_channels) {
            Hashtable<User, User> users = _channels.get(channel);
            if (users != null) {
                userArray = new User[users.size()];
                Enumeration<User> enumeration = users.elements();
                for (int i = 0; i < userArray.length; i++) {
                    User user = enumeration.nextElement();
                    userArray[i] = user;
                }
            }
        }
        return userArray;
    }


    /**
     * Returns an array of all channels that we are in.  Note that if you
     * call this method immediately after joining a new channel, the new
     * channel may not appear in this array as it is not possible to tell
     * if the join was successful until a response is received from the
     * IRC server.
     *
     * @return A String array containing the names of all channels that we
     * are in.
     * @since PircBot 1.0.0
     */
    public final String[] getChannels() {
        String[] channels = new String[0];
        synchronized (_channels) {
            channels = new String[_channels.size()];
            Enumeration<String> enumeration = _channels.keys();
            for (int i = 0; i < channels.length; i++) {
                channels[i] = enumeration.nextElement();
            }
        }
        return channels;
    }


    /**
     * Disposes of all thread resources used by this PircBot. This may be
     * useful when writing bots or clients that use multiple servers (and
     * therefore multiple PircBot instances) or when integrating a PircBot
     * with an existing program.
     * <p/>
     * Each PircBot runs its own threads for dispatching messages from its
     * outgoing message queue and receiving messages from the server.
     * Calling dispose() ensures that these threads are
     * stopped, thus freeing up system resources and allowing the PircBot
     * object to be garbage collected if there are no other references to
     * it.
     * <p/>
     * Once a PircBot object has been disposed, it should not be used again.
     * Attempting to use a PircBot that has been disposed may result in
     * unpredictable behaviour.
     *
     * @since 1.2.2
     */
    public synchronized void dispose() {
        //System.out.println("disposing...");
        if (_outputThread != null) {
            _outputThread.interrupt();
        }
        if (_inputThread != null) {
            _inputThread.dispose();
        }
    }


    /**
     * Add a user to the specified channel in our memory.
     * Overwrite the existing entry if it exists.
     */
    private void addUser(String channel, User user) {
        channel = channel.toLowerCase();
        synchronized (_channels) {
            Hashtable<User, User> users = _channels.get(channel);
            if (users == null) {
                users = new Hashtable<>();
                _channels.put(channel, users);
            }
            users.put(user, user);
        }
    }


    /**
     * Remove a user from the specified channel in our memory.
     */
    private User removeUser(String channel, String nick) {
        channel = channel.toLowerCase();
        User user = this.userFactory.create("", nick);
        synchronized (_channels) {
            Hashtable<User, User> users = _channels.get(channel);
            if (users != null) {
                return users.remove(user);
            }
        }
        return null;
    }


    /**
     * Remove a user from all channels in our memory.
     */
    private void removeUser(String nick) {
        synchronized (_channels) {
            Enumeration<String> enumeration = _channels.keys();
            while (enumeration.hasMoreElements()) {
                String channel = enumeration.nextElement();
                this.removeUser(channel, nick);
            }
        }
    }


    /**
     * Rename a user if they appear in any of the channels we know about.
     */
    private void renameUser(String oldNick, String newNick) {
        synchronized (_channels) {
            Enumeration<String> enumeration = _channels.keys();
            while (enumeration.hasMoreElements()) {
                String channel = enumeration.nextElement();
                User user = this.removeUser(channel, oldNick);
                if (user != null) {
                    user = this.userFactory.create(user.getPrefix(), newNick);
                    this.addUser(channel, user);
                }
            }
        }
    }


    /**
     * Removes an entire channel from our memory of users.
     */
    private void removeChannel(String channel) {
        channel = channel.toLowerCase(Locale.US);
        synchronized (_channels) {
            _channels.remove(channel);
        }
    }


    /**
     * Removes all channels from our memory of users.
     */
    private void removeAllChannels() {
        synchronized (_channels) {
            _channels = new Hashtable<>();
        }
    }


    private void updateUser(String channel, int userMode, String nick) {
        synchronized (_channels) {
            User newUser = null;
            Enumeration enumeration = ((ConcurrentHashMap) chatroom.getUserMap()).elements();
            while (enumeration.hasMoreElements()) {
                User userObj = (User) enumeration.nextElement();
                if (userObj.getNick().equalsIgnoreCase(nick)) {
                    switch (userMode) {
                        case OP_ADD:
                            if (userObj.hasVoice()) {
                                newUser = this.userFactory.create("@+", nick);
                            } else {
                                newUser = this.userFactory.create("@", nick);
                            }
                            break;
                        case OP_REMOVE:
                            if (userObj.hasVoice()) {
                                newUser = this.userFactory.create("+", nick);
                            } else {
                                newUser = this.userFactory.create("", nick);
                            }
                            break;
                        case VOICE_ADD:
                            if (userObj.isOp()) {
                                newUser = this.userFactory.create("@+", nick);
                            } else {
                                newUser = this.userFactory.create("+", nick);
                            }
                            break;
                        case VOICE_REMOVE:
                            if (userObj.isOp()) {
                                newUser = this.userFactory.create("@", nick);
                            } else {
                                newUser = this.userFactory.create("", nick);
                            }
                            break;
                        case HALFOP_ADD:
                            if (userObj.hasVoice()) {
                                newUser = this.userFactory.create("%+", nick);
                            } else {
                                newUser = this.userFactory.create("%", nick);
                            }
                            break;
                        case HALFOP_REMOVE:
                            if (userObj.hasVoice()) {
                                newUser = this.userFactory.create("+", nick);
                            } else {
                                newUser = this.userFactory.create("", nick);
                            }
                            break;
                    }
                }
            }

            if (newUser != null) {
                chatroom.addUser(newUser);
            } else {
                // just in case ...
                newUser = this.userFactory.create("", nick);
                chatroom.addUser(newUser);
            }
        }
    }


    // Connection stuff.
    private com.example.tom.wichatv2.Backend.Protocol.IRC.API.InputThread _inputThread = null;
    private com.example.tom.wichatv2.Backend.Protocol.IRC.API.OutputThread _outputThread = null;
    private String _charset = null;
    private InetAddress _inetAddress = null;
    private Socket _socket = null;

    // Details about the last server that we connected to.
    private String _server = null;
    private int _port = -1;
    private String _password = null;

    // Outgoing message stuff.
    private final Queue _outQueue = new com.example.tom.wichatv2.Backend.Protocol.IRC.API.Queue();
    private long _messageDelay = 1000;


    // A Hashtable of channels that points to a selfreferential Hashtable of
    // User objects (used to remember which users are in which channels).
    private Hashtable<String, Hashtable<User, User>> _channels = new Hashtable<>();

    // A Hashtable to temporarily store channel topics when we join them
    // until we find out who set that topic.
    private final Hashtable<String, String> _topics = new Hashtable<>();

    // DccManager to process and handle all DCC events.
    private int[] _dccPorts = null;
    private InetAddress _dccInetAddress = null;

    // Default settings for the PircBot.
    private boolean _autoNickChange = false;
    private int _autoNickTries = 1;
    private boolean _useSSL = true;
    private boolean _registered = false;

    private String _name = "jew";
    private final List<String> _aliases = new ArrayList<>();
    private String _nick = _name;
    private String _login = "jew";
    private String _version = "WiChat Client v1";
    private String _finger = "You ought to be arrested for fingering a bot!";

    private boolean firstPing = false;
    private final List<Character> _channelPrefixes = Arrays.asList('#', '&', '+', '!');


    // XXX: Better TLS support
    private X509TrustManager[] _trustManagers = new X509TrustManager[]{new NaiveTrustManager()};

    public void setTrustManagers(X509TrustManager[] tManagers) {
        if (tManagers == null) {
            _trustManagers = new X509TrustManager[]{new com.example.tom.wichatv2.Backend.Protocol.IRC.API.NaiveTrustManager()};
        } else {
            _trustManagers = tManagers;
        }
    }


}