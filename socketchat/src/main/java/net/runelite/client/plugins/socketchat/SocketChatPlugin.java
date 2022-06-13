package net.runelite.client.plugins.socketchat;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.VarClientStr;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarClientStrChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.socket.SocketPlugin;
import net.runelite.client.plugins.socket.org.json.JSONArray;
import net.runelite.client.plugins.socket.org.json.JSONException;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketBroadcastPacket;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;
import net.runelite.client.util.ColorUtil;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
@PluginDescriptor(
		name = "Socket Chat",
		description = "Chat over socket",
		tags = {"Socket", "chat"},
		enabledByDefault = false
)
@PluginDependency(SocketPlugin.class)
public class SocketChatPlugin extends Plugin implements KeyListener
{
	private static final Logger log = LoggerFactory.getLogger(SocketChatPlugin.class);

	@Inject
	Client client;

	@Inject
	private KeyManager keyManager;

	@Inject
	SocketChatConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private ChatMessageManager chatMessageManager;

	private boolean tradeActive = false;
	private boolean typing = false;
	private boolean lastTypingState = false;
	private HashMap<String, SentSocketMessage> sentMessages = new HashMap();
	private long time;
	private String text;
	private boolean setOverhead;

	SimpleDateFormat formatter = new SimpleDateFormat("dd/MM");
	SimpleDateFormat formatterr = new SimpleDateFormat("HH:mm");
	SimpleDateFormat formatterrr = new SimpleDateFormat("MM/dd");

	@Provides
	SocketChatConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SocketChatConfig.class);
	}

	protected void startUp() throws Exception
	{
		this.keyManager.registerKeyListener((KeyListener) this);
	}

	public void keyTyped(KeyEvent e)
	{
	}

	@Subscribe
	public void onVarClientStrChanged(VarClientStrChanged event) throws InterruptedException
	{
		this.removeHotkey();
	}

	private void removeHotkey() throws InterruptedException
	{
		String typedText = this.client.getVar(VarClientStr.CHATBOX_TYPED_TEXT);
		if (typedText.length() > 0)
		{
			String subTypedText = typedText.substring(0, typedText.length() - 1);
			String x = KeyEvent.getKeyText(this.config.hotkey().getKeyCode());
			char a = (char) KeyEvent.getExtendedKeyCodeForChar(typedText.substring(typedText.length() - 1).toCharArray()[0]);
			char b = (char) this.config.hotkey().getKeyCode();
			typedText.substring(typedText.length() - 1);
			if (a == b)
			{
				this.client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, subTypedText);
			}
		}
	}

	@Subscribe
	private void onBeforeRender(BeforeRender event)
	{
		Widget chatbox = this.client.getWidget(WidgetInfo.CHATBOX_INPUT);
		if (chatbox != null && !chatbox.isHidden())
		{
			if (!this.tradeActive && this.client.getVarcIntValue(41) == 6)
			{
				this.lastTypingState = this.typing;
				this.typing = true;
				this.tradeActive = true;
			}
			else if (this.tradeActive && this.client.getVarcIntValue(41) != 6)
			{
				this.typing = this.lastTypingState;
				this.tradeActive = false;
			}
			if (this.typing)
			{
				if (!chatbox.getText().startsWith("[SOCKET CHAT] "))
				{
					chatbox.setText("[SOCKET CHAT] " + chatbox.getText());
				}
			}
			else if (chatbox.getText().startsWith("[SOCKET CHAT] "))
			{
				chatbox.setText(chatbox.getText().substring(13));
			}
		}
	}

	public void keyPressed(KeyEvent e)
	{
		if (this.config.hotkey().matches(e))
		{
			this.typing = !this.typing;
			this.clientThread.invokeLater(() ->
				{
				try
				{
					this.removeHotkey();
				}
				catch (InterruptedException err)
				{
					err.printStackTrace();
				}
				});
		}
		if (e.getKeyCode() == 10)
		{
			String typedText = this.client.getVar(VarClientStr.CHATBOX_TYPED_TEXT);
			if (this.typing)
			{
				if (typedText.startsWith("/"))
				{
					if (!this.config.overrideSlash())
					{
						this.sendMessage(typedText);
						this.client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, "");
					}
				}
				else
				{
					this.sendMessage(typedText);
					this.client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, "");
				}
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (this.client.getGameState() == GameState.LOGGED_IN)
		{
			this.checkOverhead(true);
		}
	}

	private void sendMessage(String msg)
	{
		if (!msg.equals(""))
		{
			String sender;
			JSONArray data = new JSONArray();
			JSONObject jsonmsg = new JSONObject();
			jsonmsg.put("msg", " " + msg);
			String string = sender = this.config.getNameFake().length() >= 1 ? this.config.getNameFake() : this.client.getLocalPlayer().getName();
			if (this.config.getIcon() != 0)
			{
				jsonmsg.put("sender", "<img=" + this.config.getIcon() + ">" + sender);
			}
			else
			{
				jsonmsg.put("sender", sender);
			}
			jsonmsg.put("senderreal", this.client.getLocalPlayer().getName());
			data.put(jsonmsg);
			JSONObject send = new JSONObject();
			send.put("sChat", data);
			this.eventBus.post(new SocketBroadcastPacket(send));
			if (this.config.singleText())
			{
				this.typing = false;
			}
		}
	}

	@Subscribe
	public void onSocketReceivePacket(SocketReceivePacket event)
	{
		try
		{
			String senderReal;
			JSONObject payload = event.getPayload();
			if (!payload.has("sChat"))
			{
				return;
			}
			Date date = new Date();
			JSONArray data = payload.getJSONArray("sChat");
			JSONObject jsonmsg = data.getJSONObject(0);
			String sender = jsonmsg.getString("sender");
			String msg = jsonmsg.getString("msg");
			try
			{
				senderReal = jsonmsg.getString("senderreal");
			}
			catch (JSONException e)
			{
				senderReal = null;
			}
			ChatMessageType cmt = this.config.overrideTradeButton() ? ChatMessageType.TRADE : ChatMessageType.GAMEMESSAGE;
			Object dateTime = "";
			if (this.config.getDateStamp())
			{
				dateTime = this.config.getFreedomUnits() ? (String) dateTime + this.formatterrr.format(date) : (String) dateTime + this.formatter.format(date);
			}
			if (this.config.getTimeStamp())
			{
				dateTime = !((String) dateTime).equals("") ? (String) dateTime + " | " + this.formatterr.format(date) : (String) dateTime + this.formatterr.format(date);
			}
			String dateTimeString = "[" + (String) dateTime + "] ";
			Object customMsg = "";
			if (!this.config.showCustomMsg().equals(""))
			{
				customMsg = "[" + this.config.showCustomMsg() + "] ";
			}
			if (!((String) dateTime).equals(""))
			{
				this.client.addChatMessage(cmt, "", ColorUtil.prependColorTag((String) dateTimeString, (Color) this.config.getDateTimeColor()) + (String) customMsg + ColorUtil.prependColorTag((String) sender, (Color) this.config.getNameColor()) + ":" + ColorUtil.prependColorTag((String) msg, (Color) this.config.messageColor()), null, false);
			}
			else
			{
				this.client.addChatMessage(cmt, "", (String) customMsg + ColorUtil.prependColorTag((String) sender, (Color) this.config.getNameColor()) + ":" + ColorUtil.prependColorTag((String) msg, (Color) this.config.messageColor()), null, false);
			}
			if (this.config.splitSocketChat())
			{
				this.client.addChatMessage(ChatMessageType.PRIVATECHAT, "", "[S] " + sender + ": " + msg, null, false);
			}
			this.sentMessages.put(senderReal, new SentSocketMessage(this.client.getTickCount(), msg, false));
			this.checkOverhead(false);
		}
		catch (Exception err)
		{
			err.printStackTrace();
		}
	}

	public void keyReleased(KeyEvent e)
	{
	}

	private void checkOverhead(boolean tickCheck)
	{
		for (Player p : this.client.getPlayers())
		{
			String nameToCheck = p.getName();
			SentSocketMessage message = this.sentMessages.get(nameToCheck);
			if (message == null) continue;
			if ((long) this.client.getTickCount() > message.getTime() + 5L || !this.config.overheadText())
			{
				if (p.getOverheadText() == null || !p.getOverheadText().equals(message.getText())) continue;
				p.setOverheadText(null);
				this.sentMessages.remove(p.getName());
				continue;
			}
			if (message.isSetOverhead() || tickCheck || this.config.hideLocalPlayerOverhead() && p == this.client.getLocalPlayer() || !this.config.overheadText())
				continue;
			p.setOverheadText(message.getText());
			message.setSetOverhead(true);
		}
	}

	public long getTime()
	{
		return this.time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public String getText()
	{
		return this.text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public boolean isSetOverhead()
	{
		return this.setOverhead;
	}

	public void setSetOverhead(boolean setOverhead)
	{
		this.setOverhead = setOverhead;
	}

	private static class SentSocketMessage
	{
		private long time;
		private String text;
		private boolean setOverhead;

		public SentSocketMessage(long time, String text, boolean setOverhead)
		{
			this.time = time;
			this.text = text;
			this.setOverhead = setOverhead;
		}

		public boolean equals(Object o)
		{
			if (o == this)
			{
				return true;
			}
			if (!(o instanceof SentSocketMessage))
			{
				return false;
			}
			SentSocketMessage other = (SentSocketMessage) o;
			if (!other.canEqual(this))
			{
				return false;
			}
			if (this.getTime() != other.getTime())
			{
				return false;
			}
			if (this.isSetOverhead() != other.isSetOverhead())
			{
				return false;
			}
			String this$text = this.getText();
			String other$text = other.getText();
			return Objects.equals(this$text, other$text);
		}

		protected boolean canEqual(Object other)
		{
			return other instanceof SentSocketMessage;
		}

		public int hashCode()
		{
			int PRIME = 59;
			int result = 1;
			long $time = this.getTime();
			result = result * 59 + (int) ($time >>> 32 ^ $time);
			result = result * 59 + (this.isSetOverhead() ? 79 : 97);
			String $text = this.getText();
			return result * 59 + ($text == null ? 43 : $text.hashCode());
		}

		public String toString()
		{
			return "SocketChatPlugin.SentSocketMessage(time=" + this.getTime() + ", text=" + this.getText() + ", setOverhead=" + this.isSetOverhead() + ")";
		}

		public long getTime()
		{
			return this.time;
		}

		public void setTime(long time)
		{
			this.time = time;
		}

		public String getText()
		{
			return this.text;
		}

		public void setText(String text)
		{
			this.text = text;
		}

		public boolean isSetOverhead()
		{
			return this.setOverhead;
		}

		public void setSetOverhead(boolean setOverhead)
		{
			this.setOverhead = setOverhead;
		}
	}
}
