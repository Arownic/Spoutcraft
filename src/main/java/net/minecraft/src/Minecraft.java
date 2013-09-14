package net.minecraft.src;

import com.google.common.collect.Lists;
import com.prupe.mcpatcher.MCPatcherUtils;
import com.prupe.mcpatcher.TexturePackChangeHandler;
import com.prupe.mcpatcher.hd.AAHelper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.server.MinecraftServer;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;


// Spout Start
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.reflect.Field;

import net.minecraft.src.EntityPlayer;

import org.bukkit.ChatColor;
import org.spoutcraft.api.gui.PopupScreen;
import org.spoutcraft.api.gui.Screen;
import org.spoutcraft.api.gui.ScreenType;
import org.spoutcraft.client.SpoutClient;
import org.spoutcraft.client.chunkcache.HeightMap;
import org.spoutcraft.client.config.Configuration;
import org.spoutcraft.client.controls.SimpleKeyBindingManager;
import org.spoutcraft.client.gui.ScreenUtil;
import org.spoutcraft.client.gui.minimap.MinimapConfig;
import org.spoutcraft.client.gui.minimap.MinimapUtils;
import org.spoutcraft.client.gui.minimap.Waypoint;
import org.spoutcraft.client.io.CustomTextureManager;
import org.spoutcraft.client.packet.PacketScreenAction;
import org.spoutcraft.client.packet.ScreenAction;
import org.spoutcraft.client.packet.SpoutPacket;
import org.spoutcraft.client.spoutworth.SpoutWorth;
// Spout End

public class Minecraft implements IPlayerUsage {
	private static final ResourceLocation field_110444_H = new ResourceLocation("textures/gui/title/mojang.png");
	public static final boolean field_142025_a = Util.func_110647_a() == EnumOS.MACOS;
	private static final List field_110445_I = Lists.newArrayList(new DisplayMode[] {new DisplayMode(2560, 1600), new DisplayMode(2880, 1800)});
	/** A 10MiB preallocation to ensure the heap is reasonably sized. */
	// MCPatcher Start - Unused
	//public static byte[] memoryReserve = new byte[10485760];
	// MCPatcher End
	private final ILogAgent field_94139_O;
	private final File field_130070_K;
	private ServerData currentServerData;

	/** The RenderEngine instance used by Minecraft */
	private TextureManager renderEngine;

	/**
	 * Set to 'this' in Minecraft constructor; used by some settings get methods
	 */
	// Spout Start - private to public
	public static Minecraft theMinecraft;
	// Spout End
	public PlayerControllerMP playerController;
	private boolean fullscreen;
	private boolean hasCrashed;

	/** Instance of CrashReport. */
	private CrashReport crashReporter;
	public int displayWidth;
	public int displayHeight;
	private Timer timer;

	/** Instance of PlayerUsageSnooper. */
	private PlayerUsageSnooper usageSnooper;
	public WorldClient theWorld;
	public RenderGlobal renderGlobal;
	public EntityClientPlayerMP thePlayer;

	/**
	 * The Entity from which the renderer determines the render viewpoint. Currently is always the parent Minecraft class's
	 * 'thePlayer' instance. Modification of its location, rotation, or other settings at render time will modify the
	 * camera likewise, with the caveat of triggering chunk rebuilds as it moves, making it unsuitable for changing the
	 * viewpoint mid-render.
	 */
	public EntityLivingBase renderViewEntity;
	public EntityLivingBase pointedEntityLiving;
	public EffectRenderer effectRenderer;
	public Session session;	
	public boolean isGamePaused;	

	/** The font renderer used for displaying and measuring text. */
	public FontRenderer fontRenderer;
	public FontRenderer standardGalacticFontRenderer;

	/** The GuiScreen that's being displayed at the moment. */
	public GuiScreen currentScreen;
	public LoadingScreenRenderer loadingScreen;
	public EntityRenderer entityRenderer;

	/** Mouse left click counter */
	private int leftClickCounter = 0;

	/** Display width */
	private int tempDisplayWidth;

	/** Display height */
	private int tempDisplayHeight;

	/** Instance of IntegratedServer. */
	private IntegratedServer theIntegratedServer;

	/** Gui achievement */
	public GuiAchievement guiAchievement;
	public GuiIngame ingameGUI;

	/** Skip render world */
	public boolean skipRenderWorld;

	/** The ray trace hit that the mouse is over. */
	public MovingObjectPosition objectMouseOver;

	/** The game settings that currently hold effect. */
	public GameSettings gameSettings;	
	public SoundManager sndManager;

	/** Mouse helper instance. */
	public MouseHelper mouseHelper;
	public final File mcDataDir;
	private final File field_110446_Y;
	private final String field_110447_Z;
	private final Proxy field_110453_aa;
	private ISaveFormat saveLoader;

	/**
	 * This is set to fpsCounter every debug screen update, and is shown on the debug screen. It's also sent as part of the
	 * usage snooping.
	 */
	private static int debugFPS;

	/**
	 * When you place a block, it's set to 6, decremented once per tick, when it's 0, you can place another block.
	 */
	private int rightClickDelayTimer;

	/**
	 * Checked in Minecraft's while(running) loop, if true it's set to false and the textures refreshed.
	 */
	private boolean refreshTexturePacksScheduled;

	/** Stat file writer */
	public StatFileWriter statFileWriter;
	private String serverName;
	private int serverPort;

	/**
	 * Makes sure it doesn't keep taking screenshots when both buttons are down.
	 */
	boolean isTakingScreenshot;

	/**
	 * Does the actual gameplay have focus. If so then mouse and keys will effect the player instead of menus.
	 */
	public boolean inGameHasFocus;
	long systemTime;

	/** Join player counter */
	private int joinPlayerCounter;
	private final boolean isDemo;
	private INetworkManager myNetworkManager;
	private boolean integratedServerIsRunning;

	/** The profiler instance */
	public final Profiler mcProfiler;
	private long field_83002_am;
	private ReloadableResourceManager field_110451_am;
	private final MetadataSerializer field_110452_an;
	private List field_110449_ao;
	private DefaultResourcePack field_110450_ap;
	private ResourcePackRepository field_110448_aq;
	private LanguageManager field_135017_as;

	/**
	 * Set to true to keep the game loop running. Set to false by shutdown() to allow the game loop to exit cleanly.
	 */
	volatile boolean running;

	/** String that shows the debug information */
	public String debug;

	/** Approximate time (in ms) of last update to debug string */
	long debugUpdateTime;

	/** holds the current fps */
	int fpsCounter;
	long prevFrameTime;

	/** Profiler currently displayed in the debug screen pie chart */
	private String debugProfilerName;
	// Spout Start
	public static Thread mainThread;
	private boolean shutdown = false;
	public static boolean spoutcraftLauncher = true;
	public static boolean portable = false;
	public static int framesPerSecond = 0;
	// Spout End

	public Minecraft(Canvas par1Canvas, MinecraftApplet par2MinecraftApplet, int par3, int par4, boolean par5) {		
		MCPatcherUtils.setMinecraft(this, par6File, "1.6.2", "4.1.0_04");
		this.timer = new Timer(20.0F);
		this.usageSnooper = new PlayerUsageSnooper("client", this, MinecraftServer.func_130071_aq());
		this.systemTime = getSystemTime();
		this.mcProfiler = new Profiler();
		this.field_83002_am = -1L;
		this.field_110452_an = new MetadataSerializer();
		this.field_110449_ao = Lists.newArrayList();
		this.running = true;
		this.debug = "";
		this.debugUpdateTime = getSystemTime();
		this.prevFrameTime = -1L;
		this.debugProfilerName = "root";
		theMinecraft = this;
		this.field_94139_O = new LogAgent("Minecraft-Client", " [CLIENT]", (new File(par6File, "output-client.log")).getAbsolutePath());
		this.mcDataDir = par6File;
		this.field_110446_Y = par7File;
		this.field_130070_K = par8File;
		this.field_110447_Z = par10Str;
		this.field_110450_ap = new DefaultResourcePack(this.field_110446_Y);
		this.func_110435_P();
		this.field_110453_aa = par9Proxy;
		this.startTimerHackThread();
		this.session = par1Session;
		this.field_94139_O.logInfo("Setting user: " + par1Session.func_111285_a());
		this.field_94139_O.logInfo("(Session ID is " + par1Session.func_111286_b() + ")");
		this.isDemo = par5;
		this.displayWidth = par2;
		this.displayHeight = par3;
		this.tempDisplayWidth = par2;
		this.tempDisplayHeight = par3;
		this.fullscreen = par4;
		ImageIO.setUseCache(false);
		StatList.nopInit();
		// ToDo: this probably isn't going to work since this class no longer extends runnable
		// Spout Start
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				shutdownMinecraftApplet();
			}
		}));
		// Spout End
	}

	private void startTimerHackThread() {
		ThreadClientSleep var1 = new ThreadClientSleep(this, "Timer hack thread");
		var1.setDaemon(true);
		var1.start();
	}

	public void crashed(CrashReport par1CrashReport) {
		this.hasCrashed = true;
		this.crashReporter = par1CrashReport;
	}

	/**
	 * Wrapper around displayCrashReportInternal
	 */
	public void displayCrashReport(CrashReport par1CrashReport) {
		File var2 = new File(getMinecraft().mcDataDir, "crash-reports");
		File var3 = new File(var2, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
		System.out.println(par1CrashReport.getCompleteReport());

		if (par1CrashReport.getFile() != null) {
			System.out.println("#@!@# Game crashed! Crash report saved to: #@!@# " + par1CrashReport.getFile());
			System.exit(-1);
		} else if (par1CrashReport.saveToFile(var3, this.getLogAgent())) {
			System.out.println("#@!@# Game crashed! Crash report saved to: #@!@# " + var3.getAbsolutePath());
			System.exit(-1);
		} else {
			System.out.println("#@?@# Game crashed! Crash report could not be saved. #@?@#");
			System.exit(-2);
		}
	}

	public void setServer(String par1Str, int par2) {
		this.serverName = par1Str;
		this.serverPort = par2;
	}

	/**
	 * Starts the game: initializes the canvas, the title, the settings, etcetera.
	 */
	public void startGame() throws LWJGLException {
		this.gameSettings = new GameSettings(this, this.mcDataDir);
		if (this.gameSettings.overrideHeight > 0 && this.gameSettings.overrideWidth > 0) {
			this.displayWidth = this.gameSettings.overrideWidth;
			this.displayHeight = this.gameSettings.overrideHeight;
		}

		if (this.fullscreen) {
			Display.setFullscreen(true);
			this.displayWidth = Display.getDisplayMode().getWidth();
			this.displayHeight = Display.getDisplayMode().getHeight();

			if (this.displayWidth <= 0) {
				this.displayWidth = 1;
			}

			if (this.displayHeight <= 0) {
				this.displayHeight = 1;
			}
			// Spout Start
			Display.setDisplayMode(new DisplayMode(this.displayWidth, this.displayHeight));
			Display.setFullscreen(this.fullscreen);
			Display.setVSyncEnabled(this.gameSettings.enableVsync);
			Display.update();
			// Spout End
		} else {
			Display.setDisplayMode(new DisplayMode(this.displayWidth, this.displayHeight));
		}

		Display.setResizable(true);
		Display.setTitle("Minecraft 1.6.2");
		this.getLogAgent().logInfo("LWJGL Version: " + Sys.getVersion());

		if (Util.func_110647_a() != EnumOS.MACOS) {
			try {
				Display.setIcon(new ByteBuffer[] {this.func_110439_b(new File(this.field_110446_Y, "/icons/icon_16x16.png")), this.func_110439_b(new File(this.field_110446_Y, "/icons/icon_32x32.png"))});
			} catch (IOException var5) {
				var5.printStackTrace();
			}
		}

		try {
			Display.create(AAHelper.setupPixelFormat((new PixelFormat()).withDepthBits(24)));
		} catch (LWJGLException var4) {
			var4.printStackTrace();

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException var3) {
				;
			}

			if (this.fullscreen) {
				this.func_110441_Q();
			}

			Display.create();
		}

		// Spout Start
		System.out.println("Spoutcraft Version: " + SpoutClient.getClientVersion());
		System.out.println("Starting texture pack initialization...");
		// Spout End

		OpenGlHelper.initializeTextures();
		this.guiAchievement = new GuiAchievement(this);
		this.field_110452_an.func_110504_a(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
		this.field_110452_an.func_110504_a(new FontMetadataSectionSerializer(), FontMetadataSection.class);
		this.field_110452_an.func_110504_a(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
		this.field_110452_an.func_110504_a(new PackMetadataSectionSerializer(), PackMetadataSection.class);
		this.field_110452_an.func_110504_a(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);
		this.saveLoader = new AnvilSaveConverter(new File(this.mcDataDir, "saves"));
		this.field_110448_aq = new ResourcePackRepository(this.field_130070_K, this.field_110450_ap, this.field_110452_an, this.gameSettings);
		TexturePackChangeHandler.earlyInitialize("com.prupe.mcpatcher.TileLoader", "init");
		TexturePackChangeHandler.earlyInitialize("com.prupe.mcpatcher.ctm.CTMUtils", "reset");
		TexturePackChangeHandler.earlyInitialize("com.prupe.mcpatcher.cit.CITUtils", "init");
		TexturePackChangeHandler.earlyInitialize("com.prupe.mcpatcher.hd.FontUtils", "init");
		TexturePackChangeHandler.earlyInitialize("com.prupe.mcpatcher.mob.MobRandomizer", "init");
		TexturePackChangeHandler.earlyInitialize("com.prupe.mcpatcher.cc.Colorizer", "init");
		TexturePackChangeHandler.beforeChange1(true);
		this.field_110451_am = new SimpleReloadableResourceManager(this.field_110452_an);
		this.field_135017_as = new LanguageManager(this.field_110452_an, this.gameSettings.language);
		this.field_110451_am.func_110542_a(this.field_135017_as);
		this.func_110436_a();
		this.renderEngine = new TextureManager(this.field_110451_am);
		this.field_110451_am.func_110542_a(this.renderEngine);
		this.sndManager = new SoundManager(this.field_110451_am, this.gameSettings, this.field_110446_Y);
		this.field_110451_am.func_110542_a(this.sndManager);
		this.loadScreen();
		this.fontRenderer = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii.png"), this.renderEngine, false);

		if (this.gameSettings.language != null) {
			this.fontRenderer.setUnicodeFlag(this.field_135017_as.func_135042_a());
			this.fontRenderer.setBidiFlag(this.field_135017_as.func_135044_b());
		}

		this.standardGalacticFontRenderer = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii_sga.png"), this.renderEngine, false);
		this.field_110451_am.func_110542_a(this.fontRenderer);
		this.field_110451_am.func_110542_a(this.standardGalacticFontRenderer);
		this.field_110451_am.func_110542_a(new GrassColorReloadListener());
		this.field_110451_am.func_110542_a(new FoliageColorReloadListener());
		RenderManager.instance.itemRenderer = new ItemRenderer(this);
		this.entityRenderer = new EntityRenderer(this);
		this.statFileWriter = new StatFileWriter(this.session, this.mcDataDir);
		AchievementList.openInventory.setStatStringFormatter(new StatStringFormatKeyInv(this));
		this.mouseHelper = new MouseHelper();

		// Spout Start
		Keyboard.create();
		// Spout End

		this.checkGLError("Pre startup");
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearDepth(1.0D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		this.renderGlobal = new RenderGlobal(this);
		this.renderEngine.func_130088_a(TextureMap.field_110575_b, new TextureMap(0, "textures/blocks"));
		this.renderEngine.func_130088_a(TextureMap.field_110576_c, new TextureMap(1, "textures/items"));
		GL11.glViewport(0, 0, this.displayWidth, this.displayHeight);
		TexturePackChangeHandler.afterChange1(true);
		this.effectRenderer = new EffectRenderer(this.theWorld, this.renderEngine);

		this.checkGLError("Post startup");
		this.ingameGUI = new GuiIngame(this);

		if (this.serverName != null) {
			this.displayGuiScreen(new GuiConnecting(new GuiMainMenu(), this, this.serverName, this.serverPort));
		} else {
			// Spout Start
			this.displayGuiScreen(new org.spoutcraft.client.gui.mainmenu.MainMenu());
			// Spout End
		}

		this.loadingScreen = new LoadingScreenRenderer(this);

		if (this.gameSettings.fullScreen && !this.fullscreen) {
			this.toggleFullscreen();
		}
	}

	public void func_110436_a() {
		ArrayList var1 = Lists.newArrayList(this.field_110449_ao);
		Iterator var2 = this.field_110448_aq.func_110613_c().iterator();

		while (var2.hasNext()) {
			ResourcePackRepositoryEntry var3 = (ResourcePackRepositoryEntry)var2.next();
			var1.add(var3.func_110514_c());
		}

		this.field_135017_as.func_135043_a(var1);
		this.field_110451_am.func_110541_a(var1);

		if (this.renderGlobal != null) {
			this.renderGlobal.loadRenderers();
		}
	}

	private void func_110435_P() {
		this.field_110449_ao.add(this.field_110450_ap);
	}

	private ByteBuffer func_110439_b(File par1File) throws IOException {
		BufferedImage var2 = ImageIO.read(par1File);
		int[] var3 = var2.getRGB(0, 0, var2.getWidth(), var2.getHeight(), (int[])null, 0, var2.getWidth());
		ByteBuffer var4 = ByteBuffer.allocate(4 * var3.length);
		int[] var5 = var3;
		int var6 = var3.length;

		for (int var7 = 0; var7 < var6; ++var7) {
			int var8 = var5[var7];
			var4.putInt(var8 << 8 | var8 >> 24 & 255);
		}

		var4.flip();
		return var4;
	}

	private void func_110441_Q() throws LWJGLException {
		HashSet var1 = new HashSet();
		Collections.addAll(var1, Display.getAvailableDisplayModes());
		DisplayMode var2 = Display.getDesktopDisplayMode();

		if (!var1.contains(var2) && Util.func_110647_a() == EnumOS.MACOS) {
			Iterator var3 = field_110445_I.iterator();

			while (var3.hasNext()) {
				DisplayMode var4 = (DisplayMode)var3.next();
				boolean var5 = true;
				Iterator var6 = var1.iterator();
				DisplayMode var7;

				while (var6.hasNext()) {
					var7 = (DisplayMode)var6.next();

					if (var7.getBitsPerPixel() == 32 && var7.getWidth() == var4.getWidth() && var7.getHeight() == var4.getHeight()) {
						var5 = false;
						break;
					}
				}

				if (!var5) {
					var6 = var1.iterator();

					while (var6.hasNext()) {
						var7 = (DisplayMode)var6.next();

						if (var7.getBitsPerPixel() == 32 && var7.getWidth() == var4.getWidth() / 2 && var7.getHeight() == var4.getHeight() / 2) {
							var2 = var7;
							break;
						}
					}
				}
			}
		}

		Display.setDisplayMode(var2);
		this.displayWidth = var2.getWidth();
		this.displayHeight = var2.getHeight();
	}

	/**
	 * Displays a new screen.
	 */
	private void loadScreen() throws LWJGLException {
		ScaledResolution var1 = new ScaledResolution(this.gameSettings, this.displayWidth, this.displayHeight);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, var1.getScaledWidth_double(), var1.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
		GL11.glViewport(0, 0, this.displayWidth, this.displayHeight);
		GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_FOG);
		Tessellator var2 = Tessellator.instance;
		this.renderEngine.func_110577_a(field_110444_H);
		var2.startDrawingQuads();
		var2.setColorOpaque_I(16777215);
		var2.addVertexWithUV(0.0D, (double)this.displayHeight, 0.0D, 0.0D, 0.0D);
		var2.addVertexWithUV((double)this.displayWidth, (double)this.displayHeight, 0.0D, 0.0D, 0.0D);
		var2.addVertexWithUV((double)this.displayWidth, 0.0D, 0.0D, 0.0D, 0.0D);
		var2.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
		var2.draw();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		var2.setColorOpaque_I(16777215);
		short var3 = 256;
		short var4 = 256;
		this.scaledTessellator((var1.getScaledWidth() - var3) / 2, (var1.getScaledHeight() - var4) / 2, 0, 0, var3, var4);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		Display.update();
	}

	/**
	 * Loads Tessellator with a scaled resolution
	 */
	public void scaledTessellator(int par1, int par2, int par3, int par4, int par5, int par6) {
		float var7 = 0.00390625F;
		float var8 = 0.00390625F;
		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV((double)(par1 + 0), (double)(par2 + par6), 0.0D, (double)((float)(par3 + 0) * var7), (double)((float)(par4 + par6) * var8));
		var9.addVertexWithUV((double)(par1 + par5), (double)(par2 + par6), 0.0D, (double)((float)(par3 + par5) * var7), (double)((float)(par4 + par6) * var8));
		var9.addVertexWithUV((double)(par1 + par5), (double)(par2 + 0), 0.0D, (double)((float)(par3 + par5) * var7), (double)((float)(par4 + 0) * var8));
		var9.addVertexWithUV((double)(par1 + 0), (double)(par2 + 0), 0.0D, (double)((float)(par3 + 0) * var7), (double)((float)(par4 + 0) * var8));
		var9.draw();
	}

	/**
	 * Returns the save loader that is currently being used
	 */
	public ISaveFormat getSaveLoader() {
		return this.saveLoader;
	}

	// Spout Start
	private GuiScreen previousScreen = null;
	// Spout End

	/**
	 * Sets the argument GuiScreen as the main (topmost visible) screen.
	 */
	// Spout Start
	public void displayGuiScreen(GuiScreen screen) {
		displayGuiScreen(screen, true);
	}

	public void displayGuiScreen(GuiScreen screen, boolean notify) {
		// Part of original function
		if (screen == null && this.theWorld == null) {
			screen = new org.spoutcraft.client.gui.mainmenu.MainMenu();
		} else if (par1GuiScreen == null && this.thePlayer.func_110143_aJ() <= 0.0F) {
			screen = new GuiGameOver();
		}

		ScreenType display = ScreenUtil.getType(screen);

		if (notify && thePlayer != null && theWorld != null) {
			// Screen closed
			SpoutPacket packet = null;
			Screen widget = null;
			if (this.currentScreen != null && screen == null) {
				packet = new PacketScreenAction(ScreenAction.Close, ScreenUtil.getType(this.currentScreen));
				widget = currentScreen.getScreen();
			}
			// Screen opened
			if (screen != null && this.currentScreen == null) {
				packet = new PacketScreenAction(ScreenAction.Open, display);
				widget = screen.getScreen();
			}
			// Screen swapped
			if (screen != null && this.currentScreen != null) { // Hopefully just a submenu
				packet = new PacketScreenAction(ScreenAction.Open, display);
				widget = screen.getScreen();
			}
			boolean cancel = false;
			if (!cancel && packet != null) {
				SpoutClient.getInstance().getPacketManager().sendSpoutPacket(packet);
				if (widget instanceof PopupScreen) {
					((PopupScreen) widget).close();
				}
			}
			if (cancel) {
				return;
			}
		}
		// Spout End
		if (this.currentScreen != null) {
			this.currentScreen.onGuiClosed();
		}

		this.statFileWriter.syncStats();

		// Spout Start
		if (theWorld == null && thePlayer == null && this.ingameGUI != null) {
			// Spout End
			this.ingameGUI.getChatGUI().clearChatMessages();
		}

		// Spout Start
		if (previousScreen != null || screen != null) {
			previousScreen = this.currentScreen;
		}

		this.currentScreen = screen;

		if (screen != null) {
			// Spout End
			this.setIngameNotInFocus();
			ScaledResolution var2 = new ScaledResolution(this.gameSettings, this.displayWidth, this.displayHeight);
			int var3 = var2.getScaledWidth();
			int var4 = var2.getScaledHeight();
			// Spout Start
			screen.setWorldAndResolution(this, var3, var4);
			// Spout End
			this.skipRenderWorld = false;
		} else {
			this.setIngameFocus();
		}
	}

	// Spout Start
	public void displayPreviousScreen() {
		displayGuiScreen(previousScreen, false);
		previousScreen = null;
	}

	public void clearPreviousScreen() {
		previousScreen = null;
	}
	// Spout End

	/**
	 * Checks for an OpenGL error. If there is one, prints the error ID and error string.
	 */
	private void checkGLError(String par1Str) {
		int var2 = GL11.glGetError();

		if (var2 != 0) {
			// Spout Start
			if (!org.spoutcraft.client.gui.mainmenu.MainMenu.hasLoaded) {
				return;
			}
		}
	}

	/**
	 * Shuts down the minecraft applet by stopping the resource downloads, and clearing up GL stuff; called when the
	 * application (or web page) is exited.
	 */
	public void shutdownMinecraftApplet() {
		// Spout Start
		if (shutdown) {
			return;
		}
		shutdown = true;
		// Spout End
		try {
			this.statFileWriter.syncStats();

			this.getLogAgent().logInfo("Stopping!"); 

			try {
				this.loadWorld((WorldClient)null);
			} catch (Throwable var7) {
				;
			}

			try {
				GLAllocation.deleteTexturesAndDisplayLists();
			} catch (Throwable var6) {
				;
			}

			this.sndManager.closeMinecraft();			
		} finally {
			Display.destroy();

			if (!this.hasCrashed) {
				System.exit(0);
			}
		}

		System.gc();
	}

	public void func_99999_d() {
		this.running = true;
		CrashReport var2;

		try {
			this.startGame();
		} catch (Throwable var11) {
			var2 = CrashReport.makeCrashReport(var11, "Initializing game");
			var2.makeCategory("Initialization");
			this.displayCrashReport(this.addGraphicsAndWorldToCrashReport(var2));
			return;
		}

		try {
			while (this.running) {
				if (this.running) {
					if (this.hasCrashed && this.crashReporter != null) {
						this.displayCrashReport(this.crashReporter);
						return;
					}

					if (this.refreshTexturePacksScheduled) {
						this.refreshTexturePacksScheduled = false;
						this.func_110436_a();
					}

					try {
						this.runGameLoop();
					} catch (OutOfMemoryError var10) {
						this.freeMemory();
						this.displayGuiScreen(new GuiMemoryErrorScreen());
						System.gc();
					}
					// Spout Start
					catch (Throwable t) {
						// Try to handle errors gracefuly
						try {
							t.printStackTrace();
							this.theWorld = null;
							this.loadWorld((WorldClient) null);
							this.displayGuiScreen(new org.spoutcraft.client.gui.error.GuiUnexpectedError(t));
						} catch (Throwable failed) {
							failed.printStackTrace();
							throw new RuntimeException(t);
						}
					}
					// Spout End
					continue;
				}
			} catch (MinecraftError var12) {

			} catch (ReportedException var13) {
				this.addGraphicsAndWorldToCrashReport(var13.getCrashReport());
				this.freeMemory();
				var13.printStackTrace();
				this.displayCrashReport(var13.getCrashReport());
			} catch (Throwable var14) {
				var2 = this.addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", var14));
				this.freeMemory();
				var14.printStackTrace();
				this.displayCrashReport(var2);
			} finally {
				// Spout Start
				if (theWorld != null) {
					HeightMap map = HeightMap.getHeightMap(MinimapUtils.getWorldName());
					if (map.isDirty()) {
						map.saveThreaded();
					}
				}
				HeightMap.joinSaveThread();
				// Spout End
				this.shutdownMinecraftApplet();
			}
		}

		/**
		 * Called repeatedly from run()
		 */
		private void runGameLoop() {
			TexturePackChangeHandler.checkForTexturePackChange();

			// Spout Start
			this.checkGLError("First render check");
			// Spout End

			AxisAlignedBB.getAABBPool().cleanPool();

			// Spout Start
			mainThread = Thread.currentThread();
			if (sndManager != null) {
				sndManager.tick();
			}
			// Spout End

			if (this.theWorld != null) {
				this.theWorld.getWorldVec3Pool().clear();
			}

			this.mcProfiler.startSection("root");

			if (Display.isCloseRequested()) {
				this.shutdown();
			}

			// Spout Start
			this.checkGLError("Pre*3 render");
			// Spout End

			if (this.isGamePaused && this.theWorld != null) {
				float var1 = this.timer.renderPartialTicks;
				this.timer.updateTimer();
				this.timer.renderPartialTicks = var1;
			} else {
				this.timer.updateTimer();
			}

			// Spout Start
			this.checkGLError("Pre pre render");
			// Spout End

			long var6 = System.nanoTime();
			this.mcProfiler.startSection("tick");

			for (int var3 = 0; var3 < this.timer.elapsedTicks; ++var3) {
				this.runTick();
			}

			this.mcProfiler.endStartSection("preRenderErrors");
			long var7 = System.nanoTime() - var6;
			this.checkGLError("Pre render");
			RenderBlocks.fancyGrass = this.gameSettings.fancyGraphics;
			this.mcProfiler.endStartSection("sound");
			this.sndManager.setListener(this.thePlayer, this.timer.renderPartialTicks);

			if (!this.isGamePaused) {
				this.sndManager.func_92071_g();
			}

			this.mcProfiler.endSection();
			// Spout Start
			if (this.thePlayer != null) {
				this.mcProfiler.startSection("spoutclient");
				SpoutClient.getInstance().onTick(); // Spout - tick
				this.mcProfiler.endSection();
				// Spout End
			}

			this.mcProfiler.startSection("render");
			this.mcProfiler.startSection("display");
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			if (!Keyboard.isKeyDown(65)) {
				Display.update();
			}

			if (this.thePlayer != null && this.thePlayer.isEntityInsideOpaqueBlock()) {
				this.gameSettings.thirdPersonView = 0;
			}

			this.mcProfiler.endSection();

			if (!this.skipRenderWorld) {
				this.mcProfiler.endStartSection("gameRenderer");
				this.entityRenderer.updateCameraAndRender(this.timer.renderPartialTicks);
				this.mcProfiler.endSection();
			}

			GL11.glFlush();
			this.mcProfiler.endSection();

			if (!Display.isActive() && this.fullscreen) {
				this.toggleFullscreen();
			}

			if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart) {
				if (!this.mcProfiler.profilingEnabled) {
					this.mcProfiler.clearProfiling();
				}

				this.mcProfiler.profilingEnabled = true;
				this.displayDebugInfo(var7);
			} else {
				this.mcProfiler.profilingEnabled = false;
				this.prevFrameTime = System.nanoTime();
			}

			this.guiAchievement.updateAchievementWindow();
			this.mcProfiler.startSection("root");
			Thread.yield();

			if (Keyboard.isKeyDown(65)) {
				Display.update();
			}

			this.screenshotListener();

			if (!this.fullscreen && Display.wasResized()) {
				this.displayWidth = Display.getWidth();
				this.displayHeight = Display.getHeight();

				if (this.displayWidth <= 0) {
					this.displayWidth = 1;
				}

				if (this.displayHeight <= 0) {
					this.displayHeight = 1;
				}

				this.resize(this.displayWidth, this.displayHeight);
			}

			this.checkGLError("Post render");
			++this.fpsCounter;
			boolean var5 = this.isGamePaused;
			this.isGamePaused = this.isSingleplayer() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame() && !this.theIntegratedServer.getPublic();

			if (this.isIntegratedServerRunning() && this.thePlayer != null && this.thePlayer.sendQueue != null && this.isGamePaused != var5) {
				((MemoryConnection)this.thePlayer.sendQueue.getNetManager()).setGamePaused(this.isGamePaused);
			}

			// Spout Start
			this.checkGLError("Late render");
			// Spout End

			while (getSystemTime() >= this.debugUpdateTime + 1000L) {
				debugFPS = this.fpsCounter;
				this.debug = debugFPS + " fps, " + WorldRenderer.chunksUpdated + " chunk updates";
				WorldRenderer.chunksUpdated = 0;
				this.debugUpdateTime += 1000L;

				// Spout Start
				framesPerSecond = fpsCounter;
				checkGLError("Late render before fps");
				SpoutWorth.getInstance().updateFPS(framesPerSecond);
				checkGLError("Late render after fps");
				// Spout End

				this.fpsCounter = 0;
				this.usageSnooper.addMemoryStatsToSnooper();

				if (!this.usageSnooper.isSnooperRunning()) {
					this.usageSnooper.startSnooper();
				}
			}

			this.mcProfiler.endSection();

			if (this.func_90020_K() > 0) {
				Display.sync(EntityRenderer.performanceToFps(this.func_90020_K()));
			}
			// Spout Start
			this.checkGLError("After sync");
			// Spout End			
		}
		// Spout Start
		this.checkGLError("Game loop end");
		// Spout End		
	}

	private int func_90020_K() {
		return this.currentScreen != null && this.currentScreen instanceof GuiMainMenu ? 2 : this.gameSettings.limitFramerate;
	}

	public void freeMemory() {
		try {			
			memoryReserve = new byte[0];			
			this.renderGlobal.deleteAllDisplayLists();
		} catch (Throwable var4) {
			;
		}

		try {
			System.gc();
			AxisAlignedBB.getAABBPool().clearPool();
			this.theWorld.getWorldVec3Pool().clearAndFreeCache();
		} catch (Throwable var3) {
			;
		}

		try {
			System.gc();
			this.loadWorld((WorldClient)null);
		} catch (Throwable var2) {
			;
		}

		System.gc();
	}

	/**
	 * checks if keys are down
	 */
	private void screenshotListener() {
		if (Keyboard.isKeyDown(60)) {
			if (!this.isTakingScreenshot) {
				this.isTakingScreenshot = true;
				if (theWorld != null) {
					this.ingameGUI.getChatGUI().printChatMessage(ScreenShotHelper.saveScreenshot(this.mcDataDir, this.displayWidth, this.displayHeight));
				}
			}
		} else {
			this.isTakingScreenshot = false;
		}
	}

	/**
	 * Update debugProfilerName in response to number keys in debug screen
	 */
	private void updateDebugProfilerName(int par1) {
		List var2 = this.mcProfiler.getProfilingData(this.debugProfilerName);

		if (var2 != null && !var2.isEmpty()) {
			ProfilerResult var3 = (ProfilerResult)var2.remove(0);

			if (par1 == 0) {
				if (var3.field_76331_c.length() > 0) {
					int var4 = this.debugProfilerName.lastIndexOf(".");

					if (var4 >= 0) {
						this.debugProfilerName = this.debugProfilerName.substring(0, var4);
					}
				}
			} else {
				--par1;

				if (par1 < var2.size() && !((ProfilerResult)var2.get(par1)).field_76331_c.equals("unspecified")) {
					if (this.debugProfilerName.length() > 0) {
						this.debugProfilerName = this.debugProfilerName + ".";
					}

					this.debugProfilerName = this.debugProfilerName + ((ProfilerResult)var2.get(par1)).field_76331_c;
				}
			}
		}
	}

	private void displayDebugInfo(long par1) {
		// Spout Start - Only show if no other screens are up
		if (currentScreen != null) {
			return;
		}
		// Spout End
		if (this.mcProfiler.profilingEnabled) {
			List var3 = this.mcProfiler.getProfilingData(this.debugProfilerName);
			ProfilerResult var4 = (ProfilerResult)var3.remove(0);
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glLoadIdentity();
			GL11.glOrtho(0.0D, (double)this.displayWidth, (double)this.displayHeight, 0.0D, 1000.0D, 3000.0D);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
			GL11.glLineWidth(1.0F);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			Tessellator var5 = Tessellator.instance;
			short var6 = 160;
			int var7 = this.displayWidth - var6 - 10;
			int var8 = this.displayHeight - var6 * 2;
			GL11.glEnable(GL11.GL_BLEND);
			var5.startDrawingQuads();
			var5.setColorRGBA_I(0, 200);
			var5.addVertex((double)((float)var7 - (float)var6 * 1.1F), (double)((float)var8 - (float)var6 * 0.6F - 16.0F), 0.0D);
			var5.addVertex((double)((float)var7 - (float)var6 * 1.1F), (double)(var8 + var6 * 2), 0.0D);
			var5.addVertex((double)((float)var7 + (float)var6 * 1.1F), (double)(var8 + var6 * 2), 0.0D);
			var5.addVertex((double)((float)var7 + (float)var6 * 1.1F), (double)((float)var8 - (float)var6 * 0.6F - 16.0F), 0.0D);
			var5.draw();
			GL11.glDisable(GL11.GL_BLEND);
			double var9 = 0.0D;
			int var13;

			for (int var11 = 0; var11 < var3.size(); ++var11) {
				ProfilerResult var12 = (ProfilerResult)var3.get(var11);
				var13 = MathHelper.floor_double(var12.field_76332_a / 4.0D) + 1;
				var5.startDrawing(6);
				var5.setColorOpaque_I(var12.func_76329_a());
				var5.addVertex((double)var7, (double)var8, 0.0D);
				int var14;
				float var15;
				float var17;
				float var16;

				for (var14 = var13; var14 >= 0; --var14) {
					var15 = (float)((var9 + var12.field_76332_a * (double)var14 / (double)var13) * Math.PI * 2.0D / 100.0D);
					var16 = MathHelper.sin(var15) * (float)var6;
					var17 = MathHelper.cos(var15) * (float)var6 * 0.5F;
					var5.addVertex((double)((float)var7 + var16), (double)((float)var8 - var17), 0.0D);
				}

				var5.draw();
				var5.startDrawing(5);
				var5.setColorOpaque_I((var12.func_76329_a() & 16711422) >> 1);

				for (var14 = var13; var14 >= 0; --var14) {
					var15 = (float)((var9 + var12.field_76332_a * (double)var14 / (double)var13) * Math.PI * 2.0D / 100.0D);
					var16 = MathHelper.sin(var15) * (float)var6;
					var17 = MathHelper.cos(var15) * (float)var6 * 0.5F;
					var5.addVertex((double)((float)var7 + var16), (double)((float)var8 - var17), 0.0D);
					var5.addVertex((double)((float)var7 + var16), (double)((float)var8 - var17 + 10.0F), 0.0D);
				}

				var5.draw();
				var9 += var12.field_76332_a;
			}

			DecimalFormat var19 = new DecimalFormat("##0.00");
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			String var18 = "";

			if (!var4.field_76331_c.equals("unspecified")) {
				var18 = var18 + "[0] ";
			}

			if (var4.field_76331_c.length() == 0) {
				var18 = var18 + "ROOT ";
			} else {
				var18 = var18 + var4.field_76331_c + " ";
			}

			var13 = 16777215;
			this.fontRenderer.drawStringWithShadow(var18, var7 - var6, var8 - var6 / 2 - 16, var13);
			this.fontRenderer.drawStringWithShadow(var18 = var19.format(var4.field_76330_b) + "%", var7 + var6 - this.fontRenderer.getStringWidth(var18), var8 - var6 / 2 - 16, var13);

			for (int var21 = 0; var21 < var3.size(); ++var21) {
				ProfilerResult var20 = (ProfilerResult)var3.get(var21);
				String var22 = "";

				if (var20.field_76331_c.equals("unspecified")) {
					var22 = var22 + "[?] ";
				} else {
					var22 = var22 + "[" + (var21 + 1) + "] ";
				}

				var22 = var22 + var20.field_76331_c;
				this.fontRenderer.drawStringWithShadow(var22, var7 - var6, var8 + var6 / 2 + var21 * 8 + 20, var20.func_76329_a());
				this.fontRenderer.drawStringWithShadow(var22 = var19.format(var20.field_76332_a) + "%", var7 + var6 - 50 - this.fontRenderer.getStringWidth(var22), var8 + var6 / 2 + var21 * 8 + 20, var20.func_76329_a());
				this.fontRenderer.drawStringWithShadow(var22 = var19.format(var20.field_76330_b) + "%", var7 + var6 - this.fontRenderer.getStringWidth(var22), var8 + var6 / 2 + var21 * 8 + 20, var20.func_76329_a());
			}
		}
	}

	/**
	 * Called when the window is closing. Sets 'running' to false which allows the game loop to exit cleanly.
	 */
	public void shutdown() {
		this.running = false;
	}

	/**
	 * Will set the focus to ingame if the Minecraft window is the active with focus. Also clears any GUI screen currently
	 * displayed
	 */
	public void setIngameFocus() {
		// Spout Start
		setIngameFocus(true);
	}

	public void setIngameFocus(boolean close) {
		// Spout End
		if (Display.isActive()) {
			if (!this.inGameHasFocus) {
				this.inGameHasFocus = true;
				this.mouseHelper.grabMouseCursor();
				// Spout Start
				if (close) {
					this.displayGuiScreen((GuiScreen)null);
				}
				// Spout End
				this.leftClickCounter = 10000;
			}
		}
	}

	/**
	 * Resets the player keystate, disables the ingame focus, and ungrabs the mouse cursor.
	 */
	public void setIngameNotInFocus() {
		if (this.inGameHasFocus) {
			KeyBinding.unPressAllKeys();
			this.inGameHasFocus = false;
			this.mouseHelper.ungrabMouseCursor();
		}
	}

	/**
	 * Displays the ingame menu
	 */
	public void displayInGameMenu() {
		if (this.currentScreen == null) {
			this.displayGuiScreen(new GuiIngameMenu());

			if (this.isSingleplayer() && !this.theIntegratedServer.getPublic()) {
				this.sndManager.pauseAllSounds();
			}
		}
	}

	private void sendClickBlockToController(int par1, boolean par2) {
		if (!par2) {
			this.leftClickCounter = 0;
		}

		if (par1 != 0 || this.leftClickCounter <= 0) {
			if (par2 && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE && par1 == 0) {
				int var3 = this.objectMouseOver.blockX;
				int var4 = this.objectMouseOver.blockY;
				int var5 = this.objectMouseOver.blockZ;
				this.playerController.onPlayerDamageBlock(var3, var4, var5, this.objectMouseOver.sideHit);

				if (this.thePlayer.isCurrentToolAdventureModeExempt(var3, var4, var5)) {
					this.effectRenderer.addBlockHitEffects(var3, var4, var5, this.objectMouseOver.sideHit);
					this.thePlayer.swingItem();
				}
			} else {
				this.playerController.resetBlockRemoving();
			}
		}
	}

	/**
	 * Called whenever the mouse is clicked. Button clicked is 0 for left clicking and 1 for right clicking. Args:
	 * buttonClicked
	 */
	private void clickMouse(int par1) {
		if (par1 != 0 || this.leftClickCounter <= 0) {
			if (par1 == 0) {
				this.thePlayer.swingItem();
			}

			if (par1 == 1) {
				this.rightClickDelayTimer = 4;
			}

			boolean var2 = true;
			ItemStack var3 = this.thePlayer.inventory.getCurrentItem();

			if (this.objectMouseOver == null) {
				if (par1 == 0 && this.playerController.isNotCreative()) {
					this.leftClickCounter = 10;
				}
			} else if (this.objectMouseOver.typeOfHit == EnumMovingObjectType.ENTITY) {
				if (par1 == 0) {
					this.playerController.attackEntity(this.thePlayer, this.objectMouseOver.entityHit);
				}

				if (par1 == 1 && this.playerController.func_78768_b(this.thePlayer, this.objectMouseOver.entityHit)) {
					var2 = false;
				}
			} else if (this.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
				int var4 = this.objectMouseOver.blockX;
				int var5 = this.objectMouseOver.blockY;
				int var6 = this.objectMouseOver.blockZ;
				int var7 = this.objectMouseOver.sideHit;

				if (par1 == 0) {
					this.playerController.clickBlock(var4, var5, var6, this.objectMouseOver.sideHit);
				} else {
					int var8 = var3 != null ? var3.stackSize : 0;

					if (this.playerController.onPlayerRightClick(this.thePlayer, this.theWorld, var3, var4, var5, var6, var7, this.objectMouseOver.hitVec)) {
						var2 = false;
						this.thePlayer.swingItem();
					}

					if (var3 == null) {
						return;
					}

					if (var3.stackSize == 0) {
						this.thePlayer.inventory.mainInventory[this.thePlayer.inventory.currentItem] = null;
					} else if (var3.stackSize != var8 || this.playerController.isInCreativeMode()) {
						this.entityRenderer.itemRenderer.resetEquippedProgress();
					}
				}
			}

			if (var2 && par1 == 1) {
				ItemStack var9 = this.thePlayer.inventory.getCurrentItem();

				if (var9 != null && this.playerController.sendUseItem(this.thePlayer, this.theWorld, var9)) {
					this.entityRenderer.itemRenderer.resetEquippedProgress2();
				}
			}
		}
	}

	/**
	 * Toggles fullscreen mode.
	 */
	public void toggleFullscreen() {
		try {
			this.fullscreen = !this.fullscreen;

			if (this.fullscreen) {
				this.func_110441_Q();
				this.displayWidth = Display.getDisplayMode().getWidth();
				this.displayHeight = Display.getDisplayMode().getHeight();

				if (this.displayWidth <= 0) {
					this.displayWidth = 1;
				}

				if (this.displayHeight <= 0) {
					this.displayHeight = 1;
				}
			} else {
				Display.setDisplayMode(new DisplayMode(this.tempDisplayWidth, this.tempDisplayHeight));
				this.displayWidth = this.tempDisplayWidth;
				this.displayHeight = this.tempDisplayHeight;

				if (this.displayWidth <= 0) {
					this.displayWidth = 1;
				}

				if (this.displayHeight <= 0) {
					this.displayHeight = 1;
				}
			}

			if (this.currentScreen != null) {
				this.resize(this.displayWidth, this.displayHeight);
			}

			Display.setFullscreen(this.fullscreen);
			Display.setVSyncEnabled(this.gameSettings.enableVsync);
			Display.update();
		} catch (Exception var2) {
			var2.printStackTrace();
		}
	}

	/**
	 * Called to resize the current screen.
	 */
	// Spout Start - private to public
	public void resize(int par1, int par2) {
		// Spout End
		this.displayWidth = par1 <= 0 ? 1 : par1;
		this.displayHeight = par2 <= 0 ? 1 : par2;

		if (this.currentScreen != null) {
			ScaledResolution var3 = new ScaledResolution(this.gameSettings, par1, par2);
			int var4 = var3.getScaledWidth();
			int var5 = var3.getScaledHeight();
			this.currentScreen.setWorldAndResolution(this, var4, var5);
		}
	}

	/**
	 * Runs the current tick.
	 */
	public void runTick() {
		if (this.rightClickDelayTimer > 0) {
			--this.rightClickDelayTimer;
		}

		this.mcProfiler.startSection("stats");
		this.statFileWriter.func_77449_e();
		this.mcProfiler.endStartSection("gui");

		if (!this.isGamePaused) {
			this.ingameGUI.updateTick();
		}

		this.mcProfiler.endStartSection("pick");
		this.entityRenderer.getMouseOver(1.0F);
		this.mcProfiler.endStartSection("gameMode");

		if (!this.isGamePaused && this.theWorld != null) {
			this.playerController.updateController();
		}

		this.renderEngine.bindTexture("/terrain.png");
		this.mcProfiler.endStartSection("textures");

		if (!this.isGamePaused) {
			this.renderEngine.func_110550_d();
		}

		if (this.currentScreen == null && this.thePlayer != null) {
			if (this.thePlayer.func_110143_aJ() <= 0.0F) { //getHealth()
				this.displayGuiScreen((GuiScreen)null);
			} else if (this.thePlayer.isPlayerSleeping() && this.theWorld != null) {
				this.displayGuiScreen(new GuiSleepMP());
			}
		} else if (this.currentScreen != null && this.currentScreen instanceof GuiSleepMP && !this.thePlayer.isPlayerSleeping()) {
			this.displayGuiScreen((GuiScreen)null);
		}

		if (this.currentScreen != null) {
			this.leftClickCounter = 10000;
		}

		CrashReport var2;
		CrashReportCategory var3;

		if (this.currentScreen != null) {
			try {
				this.currentScreen.handleInput();
			} catch (Throwable var6) {
				var2 = CrashReport.makeCrashReport(var6, "Updating screen events");
				var3 = var2.makeCategory("Affected screen");
				var3.addCrashSectionCallable("Screen name", new CallableUpdatingScreenName(this));
				throw new ReportedException(var2);
			}

			if (this.currentScreen != null) {
				try {
					this.currentScreen.updateScreen();
				} catch (Throwable var5) {
					var2 = CrashReport.makeCrashReport(var5, "Ticking screen");
					var3 = var2.makeCategory("Affected screen");
					var3.addCrashSectionCallable("Screen name", new CallableParticleScreenName(this));
					throw new ReportedException(var2);
				}
			}
		}
		
		if (this.currentScreen == null || this.currentScreen.allowUserInput) {
			this.mcProfiler.endStartSection("mouse");
			int var1

			while (Mouse.next()) {
				var1 = Mouse.getEventButton();

				if (field_142025_a && var1 == 0 && (Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157))) {
					var1 = 1;
				}
				// Spout Start
				if (var1 >= 0) {
					((SimpleKeyBindingManager) SpoutClient.getInstance().getKeyBindingManager()).pressKey(Mouse.getEventButton() - 100, Mouse.getEventButtonState(), ScreenUtil.getType(currentScreen).getCode());
					this.thePlayer.handleKeyPress(Mouse.getEventButton() - 100, Mouse.getEventButtonState()); // Spout handle key press
				}
				// Spout End

				KeyBinding.setKeyBindState(var1 - 100, Mouse.getEventButtonState());

				if (Mouse.getEventButtonState()) {
					KeyBinding.onTick(var1 - 100);
				}

				long var9 = getSystemTime() - this.systemTime;

				if (var9 <= 200L) {
					int var4 = Mouse.getEventDWheel();

					if (var4 != 0) {
						this.thePlayer.inventory.changeCurrentItem(var4);

						if (this.gameSettings.noclip) {
							if (var4 > 0) {
								var4 = 1;
							}

							if (var4 < 0) {
								var4 = -1;
							}

							this.gameSettings.noclipRate += (float)var4 * 0.25F;
						}
					}

					if (this.currentScreen == null) {
						if (!this.inGameHasFocus && Mouse.getEventButtonState()) {
							this.setIngameFocus();
						}
					} else if (this.currentScreen != null) {
						this.currentScreen.handleMouseInput();
					}
				}
			}

			if (this.leftClickCounter > 0) {
				--this.leftClickCounter;
			}

			this.mcProfiler.endStartSection("keyboard");
			boolean var8;

			while (Keyboard.next()) {
				// Spout Start
				((SimpleKeyBindingManager) SpoutClient.getInstance().getKeyBindingManager()).pressKey(Keyboard.getEventKey(), Keyboard.getEventKeyState(), ScreenUtil.getType(currentScreen).getCode());
				this.thePlayer.handleKeyPress(Keyboard.getEventKey(), Keyboard.getEventKeyState()); // Spout handle key press
				// Spout End

				KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());

				if (Keyboard.getEventKeyState()) {
					KeyBinding.onTick(Keyboard.getEventKey());
				}

				if (this.field_83002_am > 0L) {
					if (getSystemTime() - this.field_83002_am >= 6000L) {
						throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
					}

					if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61)) {
						this.field_83002_am = -1L;
					}
				} else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61)) {
					this.field_83002_am = getSystemTime();
				}

				if (Keyboard.getEventKeyState()) {
					if (Keyboard.getEventKey() == 87) {
						this.toggleFullscreen();
					} else {
						if (this.currentScreen != null) {
							this.currentScreen.handleKeyboardInput();
						} else {
							if (Keyboard.getEventKey() == 1) {
								this.displayInGameMenu();
							}

							if (Keyboard.getEventKey() == 31 && Keyboard.isKeyDown(61)) {
								this.func_110436_a(); //forceReload
							}

							if (Keyboard.getEventKey() == 20 && Keyboard.isKeyDown(61)) {
								this.func_110436_a();
								//this.renderEngine.refreshTextures();
								//this.renderGlobal.loadRenderers();
							}

							if (Keyboard.getEventKey() == 33 && Keyboard.isKeyDown(61)) {
								var8 = Keyboard.isKeyDown(42) | Keyboard.isKeyDown(54);
								this.gameSettings.setOptionValue(EnumOptions.RENDER_DISTANCE, var8 ? -1 : 1);
							}

							if (Keyboard.getEventKey() == 30 && Keyboard.isKeyDown(61)) {
								this.renderGlobal.loadRenderers();
							}

							if (Keyboard.getEventKey() == 35 && Keyboard.isKeyDown(61)) {
								this.gameSettings.advancedItemTooltips = !this.gameSettings.advancedItemTooltips;
								this.gameSettings.saveOptions();
							}

							if (Keyboard.getEventKey() == 48 && Keyboard.isKeyDown(61)) {
								RenderManager.field_85095_o = !RenderManager.field_85095_o;
							}

							if (Keyboard.getEventKey() == 25 && Keyboard.isKeyDown(61)) {
								this.gameSettings.pauseOnLostFocus = !this.gameSettings.pauseOnLostFocus;
								this.gameSettings.saveOptions();
							}

							if (Keyboard.getEventKey() == 59) {
								this.gameSettings.hideGUI = !this.gameSettings.hideGUI;
							}

							if (Keyboard.getEventKey() == 61) {
								this.gameSettings.showDebugInfo = !this.gameSettings.showDebugInfo;
								this.gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
							}

							if (Keyboard.getEventKey() == 63) {
								++this.gameSettings.thirdPersonView;

								if (this.gameSettings.thirdPersonView > 2) {
									this.gameSettings.thirdPersonView = 0;
								}
							}

							if (Keyboard.getEventKey() == 66) {
								this.gameSettings.smoothCamera = !this.gameSettings.smoothCamera;
							}
						}

						int var9;
						// Spout Start
						if (Configuration.isHotbarQuickKeysEnabled()) {
							for (var9 = 0; var9 < 9; ++var9) {
								if (Keyboard.getEventKey() == 2 + var9) {
									this.thePlayer.inventory.currentItem = var9;
								}
							}
						}
						// Spout End

						if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart) {
							if (Keyboard.getEventKey() == 11) {
								this.updateDebugProfilerName(0);
							}

							for (var9 = 0; var9 < 9; ++var9) {
								if (Keyboard.getEventKey() == 2 + var9) {
									this.updateDebugProfilerName(var9 + 1);
								}
							}
						}
					}
				}
			}

			var8 = this.gameSettings.chatVisibility != 2;

			while (this.gameSettings.keyBindInventory.isPressed()) {
				if (this.playerController.func_110738_j()) {
					this.thePlayer.func_110322_i();
				} else {
					this.displayGuiScreen(new GuiInventory(this.thePlayer));
				}
			}

			while (this.gameSettings.keyBindDrop.isPressed()) {
				this.thePlayer.dropOneItem(GuiScreen.isCtrlKeyDown());
			}

			while (this.gameSettings.keyBindChat.isPressed() && var8) {
				this.displayGuiScreen(new GuiChat());
			}

			// Spout Start - Open chat in SP with debug key
			if (this.currentScreen == null && this.gameSettings.keyBindCommand.isPressed() && var8 && Keyboard.getEventKey() != Keyboard.KEY_SLASH && isIntegratedServerRunning()) {
				this.displayGuiScreen(new GuiChat());
				thePlayer.sendChatMessage(ChatColor.RED + "Debug Console Opened");
			}

			if (currentScreen == null && Keyboard.getEventKey() == Keyboard.KEY_SLASH) {
				// Spout End
				this.displayGuiScreen(new GuiChat("/"));
			}

			if (this.thePlayer.isUsingItem()) {
				if (!this.gameSettings.keyBindUseItem.pressed) {
					this.playerController.onStoppedUsingItem(this.thePlayer);
				}

				label381:

					while (true) {
						if (!this.gameSettings.keyBindAttack.isPressed()) {
							while (this.gameSettings.keyBindUseItem.isPressed()) {
								;
							}

							while (true) {
								if (this.gameSettings.keyBindPickBlock.isPressed()) {
									continue;
								}

								break label381;
							}
						}
					}
			} else {
				while (this.gameSettings.keyBindAttack.isPressed()) {
					this.clickMouse(0);
				}

				while (this.gameSettings.keyBindUseItem.isPressed()) {
					this.clickMouse(1);
				}

				while (this.gameSettings.keyBindPickBlock.isPressed()) {
					this.clickMiddleMouseButton();
				}
			}

			if (this.gameSettings.keyBindUseItem.pressed && this.rightClickDelayTimer == 0 && !this.thePlayer.isUsingItem()) {
				this.clickMouse(1);
			}

			this.sendClickBlockToController(0, this.currentScreen == null && this.gameSettings.keyBindAttack.pressed && this.inGameHasFocus);
		}

		if (this.theWorld != null) {
			if (this.thePlayer != null) {
				++this.joinPlayerCounter;

				if (this.joinPlayerCounter == 30) {
					this.joinPlayerCounter = 0;
					this.theWorld.joinEntityInSurroundings(this.thePlayer);
				}
			}

			this.mcProfiler.endStartSection("gameRenderer");

			if (!this.isGamePaused) {
				this.entityRenderer.updateRenderer();
			}

			this.mcProfiler.endStartSection("levelRenderer");

			if (!this.isGamePaused) {
				this.renderGlobal.updateClouds();
			}

			this.mcProfiler.endStartSection("level");

			if (!this.isGamePaused) {
				if (this.theWorld.lastLightningBolt > 0) {
					--this.theWorld.lastLightningBolt;
				}

				this.theWorld.updateEntities();
			}

			if (!this.isGamePaused) {
				this.theWorld.setAllowedSpawnTypes(this.theWorld.difficultySetting > 0, true);

				try {
					this.theWorld.tick();
				} catch (Throwable var7) {
					var2 = CrashReport.makeCrashReport(var7, "Exception in world tick");

					if (this.theWorld == null) {
						var3 = var2.makeCategory("Affected level");
						var3.addCrashSection("Problem", "Level is null!");
					} else {
						this.theWorld.addWorldInfoToCrashReport(var2);
					}

					throw new ReportedException(var2);
				}
			}

			this.mcProfiler.endStartSection("animateTick");

			if (!this.isGamePaused && this.theWorld != null) {
				this.theWorld.doVoidFogParticles(MathHelper.floor_double(this.thePlayer.posX), MathHelper.floor_double(this.thePlayer.posY), MathHelper.floor_double(this.thePlayer.posZ));
			}

			this.mcProfiler.endStartSection("particles");

			if (!this.isGamePaused) {
				this.effectRenderer.updateEffects();
			}
		} else if (this.myNetworkManager != null) {
			this.mcProfiler.endStartSection("pendingConnection");
			this.myNetworkManager.processReadPackets();
		}

		this.mcProfiler.endSection();
		this.systemTime = getSystemTime();
	}
	
	/**
	 * Arguments: World foldername,  World ingame name, WorldSettings
	 */
	public void launchIntegratedServer(String par1Str, String par2Str, WorldSettings par3WorldSettings) {
		this.loadWorld((WorldClient)null);
		System.gc();
		ISaveHandler var4 = this.saveLoader.getSaveLoader(par1Str, false);
		WorldInfo var5 = var4.loadWorldInfo();

		if (var5 == null && par3WorldSettings != null) {			
			var5 = new WorldInfo(par3WorldSettings, par1Str);
			var4.saveWorldInfo(var5);
		}

		if (par3WorldSettings == null) {
			par3WorldSettings = new WorldSettings(var5);
		}

		this.statFileWriter.readStat(StatList.startGameStat, 1);
		this.theIntegratedServer = new IntegratedServer(this, par1Str, par2Str, par3WorldSettings);
		this.theIntegratedServer.startServerThread();
		this.integratedServerIsRunning = true;
		this.loadingScreen.displayProgressMessage(I18n.func_135053_a("menu.loadingLevel"));

		while (!this.theIntegratedServer.serverIsInRunLoop()) {
			String var6 = this.theIntegratedServer.getUserMessage();

			if (var6 != null) {
				this.loadingScreen.resetProgresAndWorkingMessage(I18n.func_135053_a(var6));
			} else {
				this.loadingScreen.resetProgresAndWorkingMessage("");
			}

			try {
				Thread.sleep(200L);
			} catch (InterruptedException var9) {
				;
			}
		}

		this.displayGuiScreen((GuiScreen)null);

		try {
			NetClientHandler var10 = new NetClientHandler(this, this.theIntegratedServer);
			this.myNetworkManager = var10.getNetManager();
		} catch (IOException var8) {
			this.displayCrashReport(this.addGraphicsAndWorldToCrashReport(new CrashReport("Connecting to integrated server", var8)));
		}
	}

	/**
	 * unloads the current world first
	 */
	public void loadWorld(WorldClient par1WorldClient) {
		this.loadWorld(par1WorldClient, "");
	}

	/**
	 * par2Str is displayed on the loading screen to the user unloads the current world first
	 */
	public void loadWorld(WorldClient par1WorldClient, String par2Str) {
		this.statFileWriter.syncStats();

		if (par1WorldClient == null) {
			NetClientHandler var3 = this.getNetHandler();

			if (var3 != null) {
				var3.cleanup();
			}

			if (this.myNetworkManager != null) {
				this.myNetworkManager.closeConnections();
			}

			if (this.theIntegratedServer != null) {
				this.theIntegratedServer.initiateShutdown();
			}

			this.theIntegratedServer = null;
		}

		this.renderViewEntity = null;
		this.myNetworkManager = null;

		if (this.loadingScreen != null) {
			this.loadingScreen.resetProgressAndMessage(par2Str);
			this.loadingScreen.resetProgresAndWorkingMessage("");
		}

		if (par1WorldClient == null && this.theWorld != null) {
			this.setServerData((ServerData)null);
			this.integratedServerIsRunning = false;
		}

		this.sndManager.playStreaming((String)null, 0.0F, 0.0F, 0.0F);
		this.sndManager.stopAllSounds();
		this.theWorld = par1WorldClient;

		if (par1WorldClient != null) {
			if (this.renderGlobal != null) {
				this.renderGlobal.setWorldAndLoadRenderers(par1WorldClient);
			}

			if (this.effectRenderer != null) {
				this.effectRenderer.clearEffects(par1WorldClient);
			}

			if (this.thePlayer == null) {
				this.thePlayer = this.playerController.func_78754_a(par1WorldClient);
				this.playerController.flipPlayer(this.thePlayer);
			}

			this.thePlayer.preparePlayerToSpawn();
			par1WorldClient.spawnEntityInWorld(this.thePlayer);
			this.thePlayer.movementInput = new MovementInputFromOptions(this.gameSettings);
			this.playerController.setPlayerCapabilities(this.thePlayer);
			this.renderViewEntity = this.thePlayer;
			// Spout Start
			SpoutClient.getInstance().onWorldEnter();
			// Spout End
		} else {
			this.saveLoader.flushCache();
			this.thePlayer = null;
			// Spout Start
			if (renderEngine.oldPack != null) {
				renderEngine.texturePack.setTexturePack(renderEngine.oldPack);
				renderEngine.oldPack = null;
			}
			//renderEngine.refreshTextures(); // Nope, lets not do this again...
			SpoutClient.getInstance().onWorldExit();
			SpoutClient.getInstance().clearPermissions();
			// Spout End
		}

		System.gc();
		this.systemTime = 0L;
	}

	/**
	 * A String of renderGlobal.getDebugInfoRenders
	 */
	public String debugInfoRenders() {
		return this.renderGlobal.getDebugInfoRenders();
	}

	/**
	 * Gets the information in the F3 menu about how many entities are infront/around you
	 */
	public String getEntityDebug() {
		return this.renderGlobal.getDebugInfoEntities();
	}

	/**
	 * Gets the name of the world's current chunk provider
	 */
	public String getWorldProviderName() {
		return this.theWorld.getProviderName();
	}

	/**
	 * A String of how many entities are in the world
	 */
	public String debugInfoEntities() {
		return "P: " + this.effectRenderer.getStatistics() + ". T: " + this.theWorld.getDebugLoadedEntities();
	}

	public void setDimensionAndSpawnPlayer(int par1) {
		this.theWorld.setSpawnLocation();
		this.theWorld.removeAllEntities();
		int var2 = 0;
		String var3 = null;

		if (this.thePlayer != null) {
			var2 = this.thePlayer.entityId;
			this.theWorld.removeEntity(this.thePlayer);
			var3 = this.thePlayer.func_142021_k();
		}

		this.renderViewEntity = null;
		this.thePlayer = this.playerController.func_78754_a(this.theWorld);
		this.thePlayer.dimension = par1;
		this.renderViewEntity = this.thePlayer;
		this.thePlayer.preparePlayerToSpawn();
		this.thePlayer.func_142020_c(var3);
		this.theWorld.spawnEntityInWorld(this.thePlayer);
		this.playerController.flipPlayer(this.thePlayer);
		this.thePlayer.movementInput = new MovementInputFromOptions(this.gameSettings);
		this.thePlayer.entityId = var2;
		this.playerController.setPlayerCapabilities(this.thePlayer);

		// Spout Start
		EntityPlayer var9 = this.thePlayer;
		if (var9 != null) {
			this.thePlayer.setData(var9.getData()); // Even in MP still need to copy Spout data across
			if (var9.health <= 0) {
				String name = "Death " + new SimpleDateFormat("dd-MM-yyyy").format(new Date());
				Waypoint death = new Waypoint(name, (int)var9.posX, (int)var9.posY, (int)var9.posZ, true);
				death.deathpoint = true;
				MinimapConfig.getInstance().addWaypoint(death);
			}
		}
		// Spout End

		if (this.currentScreen instanceof GuiGameOver) {
			this.displayGuiScreen((GuiScreen)null);
		}
	}

	/**
	 * Gets whether this is a demo or not.
	 */
	public final boolean isDemo() {
		return this.isDemo;
	}

	/**
	 * Returns the NetClientHandler.
	 */
	public NetClientHandler getNetHandler() {
		return this.thePlayer != null ? this.thePlayer.sendQueue : null;
	}
	
	public static boolean isGuiEnabled() {
		return theMinecraft == null || !theMinecraft.gameSettings.hideGUI;
	}

	public static boolean isFancyGraphicsEnabled() {
		return theMinecraft != null && theMinecraft.gameSettings.fancyGraphics;
	}

	/**
	 * Returns if ambient occlusion is enabled
	 */
	public static boolean isAmbientOcclusionEnabled() {
		// Spout Start
		return theMinecraft != null && Configuration.ambientOcclusion;
		// Spout End
	}

	public static boolean isDebugInfoEnabled() {
		return theMinecraft != null && theMinecraft.gameSettings.showDebugInfo;
	}

	/**
	 * Returns true if the message is a client command and should not be sent to the server. However there are no such
	 * commands at this point in time.
	 */
	public boolean handleClientCommand(String par1Str) {
		return false;
	}

	/**
	 * Called when the middle mouse button gets clicked
	 */
	private void clickMiddleMouseButton() {
		if (this.objectMouseOver != null) {
			boolean var1 = this.thePlayer.capabilities.isCreativeMode;
			int var3 = 0;
			boolean var4 = false;
			int var2;
			int var5;

			if (this.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
				var5 = this.objectMouseOver.blockX;
				int var6 = this.objectMouseOver.blockY;
				int var7 = this.objectMouseOver.blockZ;
				Block var8 = Block.blocksList[this.theWorld.getBlockId(var5, var6, var7)];

				if (var8 == null) {
					return;
				}

				var2 = var8.idPicked(this.theWorld, var5, var6, var7);

				if (var2 == 0) {
					return;
				}

				var4 = Item.itemsList[var2].getHasSubtypes();
				int var9 = var2 < 256 && !Block.blocksList[var8.blockID].isFlowerPot() ? var2 : var8.blockID;
				var3 = Block.blocksList[var9].getDamageValue(this.theWorld, var5, var6, var7);
			} else {
				if (this.objectMouseOver.typeOfHit != EnumMovingObjectType.ENTITY || this.objectMouseOver.entityHit == null || !var1) {
					return;
				}

				if (this.objectMouseOver.entityHit instanceof EntityPainting) {
					var2 = Item.painting.itemID;
				} else if (this.objectMouseOver.entityHit instanceof EntityLeashKnot) {
					var2 = Item.field_111214_ch.itemID;
				} else if (this.objectMouseOver.entityHit instanceof EntityItemFrame) {
					EntityItemFrame var10 = (EntityItemFrame)this.objectMouseOver.entityHit;

					if (var10.getDisplayedItem() == null) {
						var2 = Item.itemFrame.itemID;
					} else {
						var2 = var10.getDisplayedItem().itemID;
						var3 = var10.getDisplayedItem().getItemDamage();
						var4 = true;
					}
				} else if (this.objectMouseOver.entityHit instanceof EntityMinecart) {
					EntityMinecart var11 = (EntityMinecart)this.objectMouseOver.entityHit;

					if (var11.getMinecartType() == 2) {
						var2 = Item.minecartPowered.itemID;
					} else if (var11.getMinecartType() == 1) {
						var2 = Item.minecartCrate.itemID;
					} else if (var11.getMinecartType() == 3) {
						var2 = Item.minecartTnt.itemID;
					} else if (var11.getMinecartType() == 5) {
						var2 = Item.minecartHopper.itemID;
					} else {
						var2 = Item.minecartEmpty.itemID;
					}
				} else if (this.objectMouseOver.entityHit instanceof EntityBoat) {
					var2 = Item.boat.itemID;
				} else {
					var2 = Item.monsterPlacer.itemID;
					var3 = EntityList.getEntityID(this.objectMouseOver.entityHit);
					var4 = true;

					if (var3 <= 0 || !EntityList.entityEggs.containsKey(Integer.valueOf(var3))) {
						return;
					}
				}
			}

			this.thePlayer.inventory.setCurrentItem(var2, var3, var4, var1);

			if (var1) {
				var5 = this.thePlayer.inventoryContainer.inventorySlots.size() - 9 + this.thePlayer.inventory.currentItem;
				this.playerController.sendSlotPacket(this.thePlayer.inventory.getStackInSlot(this.thePlayer.inventory.currentItem), var5);
			}
		}
	}

	/**
	 * adds core server Info (GL version , Texture pack, isModded, type), and the worldInfo to the crash report
	 */
	public CrashReport addGraphicsAndWorldToCrashReport(CrashReport par1CrashReport) {
		par1CrashReport.func_85056_g().addCrashSectionCallable("Launched Version", new CallableLaunchedVersion(this));
		par1CrashReport.func_85056_g().addCrashSectionCallable("LWJGL", new CallableLWJGLVersion(this));
		par1CrashReport.func_85056_g().addCrashSectionCallable("OpenGL", new CallableGLInfo(this));
		par1CrashReport.func_85056_g().addCrashSectionCallable("Is Modded", new CallableModded(this));
		par1CrashReport.func_85056_g().addCrashSectionCallable("Type", new CallableType2(this));
		par1CrashReport.func_85056_g().addCrashSectionCallable("Resource Pack", new CallableTexturePack(this));
		par1CrashReport.func_85056_g().addCrashSectionCallable("Current Language", new CallableClientProfiler(this));
		par1CrashReport.func_85056_g().addCrashSectionCallable("Profiler Position", new CallableClientMemoryStats(this));
		par1CrashReport.func_85056_g().addCrashSectionCallable("Vec3 Pool Size", new MinecraftINNER13(this));

		if (this.theWorld != null) {
			this.theWorld.addWorldInfoToCrashReport(par1CrashReport);
		}

		return par1CrashReport;
	}

	/**
	 * Return the singleton Minecraft instance for the game
	 */
	public static Minecraft getMinecraft() {
		return theMinecraft;
	}

	public void addServerStatsToSnooper(PlayerUsageSnooper par1PlayerUsageSnooper) {
		par1PlayerUsageSnooper.addData("fps", Integer.valueOf(debugFPS));
		par1PlayerUsageSnooper.addData("texpack_name", this.field_110448_aq.func_110610_d());
		par1PlayerUsageSnooper.addData("vsync_enabled", Boolean.valueOf(this.gameSettings.enableVsync));
		par1PlayerUsageSnooper.addData("display_frequency", Integer.valueOf(Display.getDisplayMode().getFrequency()));
		par1PlayerUsageSnooper.addData("display_type", this.fullscreen ? "fullscreen" : "windowed");
		par1PlayerUsageSnooper.addData("run_time", Long.valueOf((MinecraftServer.func_130071_aq() - par1PlayerUsageSnooper.func_130105_g()) / 60L * 1000L));

		if (this.theIntegratedServer != null && this.theIntegratedServer.getPlayerUsageSnooper() != null) {
			par1PlayerUsageSnooper.addData("snooper_partner", this.theIntegratedServer.getPlayerUsageSnooper().getUniqueID());
		}
	}

	public void addServerTypeToSnooper(PlayerUsageSnooper par1PlayerUsageSnooper) {
		par1PlayerUsageSnooper.addData("opengl_version", GL11.glGetString(GL11.GL_VERSION));
		par1PlayerUsageSnooper.addData("opengl_vendor", GL11.glGetString(GL11.GL_VENDOR));
		par1PlayerUsageSnooper.addData("client_brand", ClientBrandRetriever.getClientModName());
		par1PlayerUsageSnooper.addData("launched_version", this.field_110447_Z);
		ContextCapabilities var2 = GLContext.getCapabilities();
		par1PlayerUsageSnooper.addData("gl_caps[ARB_multitexture]", Boolean.valueOf(var2.GL_ARB_multitexture));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_multisample]", Boolean.valueOf(var2.GL_ARB_multisample));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_texture_cube_map]", Boolean.valueOf(var2.GL_ARB_texture_cube_map));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_vertex_blend]", Boolean.valueOf(var2.GL_ARB_vertex_blend));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_matrix_palette]", Boolean.valueOf(var2.GL_ARB_matrix_palette));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_vertex_program]", Boolean.valueOf(var2.GL_ARB_vertex_program));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_vertex_shader]", Boolean.valueOf(var2.GL_ARB_vertex_shader));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_fragment_program]", Boolean.valueOf(var2.GL_ARB_fragment_program));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_fragment_shader]", Boolean.valueOf(var2.GL_ARB_fragment_shader));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_shader_objects]", Boolean.valueOf(var2.GL_ARB_shader_objects));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_vertex_buffer_object]", Boolean.valueOf(var2.GL_ARB_vertex_buffer_object));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_framebuffer_object]", Boolean.valueOf(var2.GL_ARB_framebuffer_object));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_pixel_buffer_object]", Boolean.valueOf(var2.GL_ARB_pixel_buffer_object));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_uniform_buffer_object]", Boolean.valueOf(var2.GL_ARB_uniform_buffer_object));
		par1PlayerUsageSnooper.addData("gl_caps[ARB_texture_non_power_of_two]", Boolean.valueOf(var2.GL_ARB_texture_non_power_of_two));
		par1PlayerUsageSnooper.addData("gl_caps[gl_max_vertex_uniforms]", Integer.valueOf(GL11.glGetInteger(GL20.GL_MAX_VERTEX_UNIFORM_COMPONENTS)));
		par1PlayerUsageSnooper.addData("gl_caps[gl_max_fragment_uniforms]", Integer.valueOf(GL11.glGetInteger(GL20.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS)));
		par1PlayerUsageSnooper.addData("gl_max_texture_size", Integer.valueOf(getGLMaximumTextureSize()));
	}

	/**
	 * Used in the usage snooper.
	 */
	public static int getGLMaximumTextureSize() {
		for (int var0 = 16384; var0 > 0; var0 >>= 1) {
			GL11.glTexImage2D(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_RGBA, var0, var0, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
			int var1 = GL11.glGetTexLevelParameteri(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);

			if (var1 != 0) {
				return var0;
			}
		}

		return -1;
	}

	/**
	 * Returns whether snooping is enabled or not.
	 */
	public boolean isSnooperEnabled() {
		return this.gameSettings.snooperEnabled;
	}

	/**
	 * Set the current ServerData instance.
	 */
	public void setServerData(ServerData par1ServerData) {
		this.currentServerData = par1ServerData;
	}

	public boolean isIntegratedServerRunning() {
		return this.integratedServerIsRunning;
	}

	/**
	 * Returns true if there is only one player playing, and the current server is the integrated one.
	 */
	public boolean isSingleplayer() {
		return this.integratedServerIsRunning && this.theIntegratedServer != null;
	}

	/**
	 * Returns the currently running integrated server
	 */
	public IntegratedServer getIntegratedServer() {
		return this.theIntegratedServer;
	}

	public static void stopIntegratedServer() {
		if (theMinecraft != null) {
			IntegratedServer var0 = theMinecraft.getIntegratedServer();

			if (var0 != null) {
				var0.stopServer();
			}
		}
	}

	/**
	 * Returns the PlayerUsageSnooper instance.
	 */
	public PlayerUsageSnooper getPlayerUsageSnooper() {
		return this.usageSnooper;
	}

	/**
	 * Gets the system time in milliseconds.
	 */
	public static long getSystemTime() {
		return Sys.getTime() * 1000L / Sys.getTimerResolution();
	}

	/**
	 * Returns whether we're in full screen or not.
	 */
	public boolean isFullScreen() {
		return this.fullscreen;
	}

	public ILogAgent getLogAgent() {
		return this.field_94139_O;
	}
	
	public Session func_110432_I() {
		return this.session;
	}

	public Proxy func_110437_J() {
		return this.field_110453_aa;
	}

	public TextureManager func_110434_K() {
		return this.renderEngine;
	}

	public ResourceManager func_110442_L() {
		return this.field_110451_am;
	}

	public ResourcePackRepository func_110438_M() {
		return this.field_110448_aq;
	}

	public LanguageManager func_135016_M() {
		return this.field_135017_as;
	}

	static String func_110431_a(Minecraft par0Minecraft) {
		return par0Minecraft.field_110447_Z;
	}

	static LanguageManager func_142024_b(Minecraft par0Minecraft) {
		return par0Minecraft.field_135017_as;
	}

	// Spout Start
	public boolean isMultiplayerWorld() {
		return theWorld != null && theWorld.isRemote;
	}
	// Spout End
}