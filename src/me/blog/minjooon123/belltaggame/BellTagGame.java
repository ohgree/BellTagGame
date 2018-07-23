package me.blog.minjooon123.belltaggame;

import java.util.*;
import java.util.logging.Logger;

import com.sun.tools.javac.tree.JCTree;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;


public class BellTagGame extends JavaPlugin{
	public static boolean isSwordGiven;
	public static int swordType;
	public static float walkVolume;
	public static float sprintVolume;
	public static float pitch;
	public static boolean markerEnabled;
	public static int markerDelay;
	public static boolean taggerContagious;
	public static boolean invisibleNametag;
	public static boolean winCounter;
	public static int timeToWin;
	public static ScoreboardManager manager;
	public static Scoreboard board;
	public static Team taggerTeam;
	public static Team runnerTeam;
    public static BellTagGame instance;

	public static HashMap<String, Integer> playerBellCount = new HashMap<String, Integer>();
	public static List<String> taggers = new ArrayList<String>();
	//public static int taggerNum = 0;
	public static boolean isEnabled = false;
	private final Logger logger = Logger.getLogger("Minecraft");
	private final TagListener tagListener = new TagListener(this);

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();

		for(World w: Bukkit.getWorlds()) {
			for(Entity e: w.getEntities()) {
				if(e.hasMetadata("BTmarker")) e.remove();
			}
		}

		this.logger.info("[BellTagGame] Disabled BellTagGame v" + pdfFile.getVersion());
	}

	@Override
	public void onEnable() {
		instance = this;

		PluginDescriptionFile pdfFile = this.getDescription();
		this.getConfig().options().copyDefaults(true);
		isSwordGiven = this.getConfig().getBoolean("BellTag.Give Swords to Taggers", false);
		swordType = this.getConfig().getInt("BellTag.Sword Type", 3);
		walkVolume = (float) this.getConfig().getDouble("BellTag.Walking Volume", 0.5);
		sprintVolume = (float) this.getConfig().getDouble("BellTag.Sprinting Volume", 1);
		pitch = (float) this.getConfig().getDouble("BellTag.Bell Pitch", 1.95);
		markerEnabled = this.getConfig().getBoolean("BellTag.Enable Marker", false);
		markerDelay = this.getConfig().getInt("BellTag.Marker Delay In Minutes", 2);
		taggerContagious = this.getConfig().getBoolean("BellTag.Tagger on Death", false);
		invisibleNametag = this.getConfig().getBoolean("BellTag.Nametag Invisibility", false);
		winCounter = this.getConfig().getBoolean("BellTag.Time to Win Enabled", false);
		timeToWin = this.getConfig().getInt("BellTag.Time to Win In Minutes", 10);
		this.saveConfig();
		this.logger.info("[BellTagGame] Successfully Loaded Config for BellTagGame");

		this.getServer().getPluginManager().registerEvents(tagListener, this);
		this.logger.info("[BellTagGame] Registered Event Listener");

		manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();

		BellTagGame.taggerTeam = BellTagGame.board.registerNewTeam("BTtagger");
		BellTagGame.runnerTeam = BellTagGame.board.registerNewTeam("BTrunner");

		BellTagGame.taggerTeam.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
		BellTagGame.runnerTeam.setNameTagVisibility(NameTagVisibility.NEVER);
		BellTagGame.taggerTeam.setCanSeeFriendlyInvisibles(true);
		BellTagGame.taggerTeam.setPrefix("" + ChatColor.RED + ChatColor.BOLD);
		BellTagGame.runnerTeam.setPrefix("" + ChatColor.AQUA + ChatColor.BOLD);
		BellTagGame.taggerTeam.setAllowFriendlyFire(false);
		BellTagGame.runnerTeam.setAllowFriendlyFire(false);
		this.logger.info("[BellTagGame] Team support ready");

		this.logger.info("[BellTagGame] Enabled BellTagGame v" + pdfFile.getVersion());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		} // checks whether the sender is a player or a console
		if(cmd.getName().equalsIgnoreCase("belltag")) {
			if(args.length == 0) {// belltag
				if(player != null) {
					player.sendMessage(ChatColor.GOLD + "/belltag start [number]"
							+ ChatColor.GRAY + ": "
							+ ChatColor.AQUA + "술래잡기를 시작합니다. 술래 목록이 비어있는 경우, 해당 수만큼 플레이어 중 랜덤 선택합니다.");
					player.sendMessage(ChatColor.GOLD + "/belltag add <player>"
							+ ChatColor.GRAY + ": "
							+ ChatColor.AQUA + "해당 플레이어를 술래 목록에 추가합니다.");
					player.sendMessage(ChatColor.GOLD + "/belltag clearlist"
							+ ChatColor.GRAY + ": "
							+ ChatColor.AQUA + "술래 목록을 초기화시킵니다.");
					player.sendMessage(ChatColor.GOLD + "/belltag info"
							+ ChatColor.GRAY + ": "
							+ ChatColor.AQUA + "플러그인에 대해 쓸데없이 긴 설명을 해줍니다.");
					player.sendMessage(ChatColor.GOLD + "/belltag stop"
							+ ChatColor.GRAY + ": "
							+ ChatColor.AQUA + "플러그인을 멈춥니다. 멈춘 상태에서는 술래를 변경할 수 있습니다.");
				}
				else {
					this.logger.info("/belltag start [number]: 방울 술래잡기를 시작합니다. 술래 목록이 비어있는 경우, "
							+ "해당 수만큼 플레이어 중 랜덤 선택합니다.");
					this.logger.info("/belltag add <player>: 해당 플레이어를 술래 목록에 추가합니다.");
					this.logger.info("/belltag clearlist: 술래 목록을 초기화시킵니다.");
					this.logger.info("/belltag info: 플러그인에 대해 쓸데없이 긴 설명을 해줍니다.");
					this.logger.info("/belltag stop: 플러그인을 멈춥니다. 멈춘 상태에서는 술래 리스트를 변경할 수 있습니다.");
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase("info") && args.length == 1) {// belltag info
				if(player != null) {
					player.sendMessage(ChatColor.BLUE + "[방울 술래잡기]");
					player.sendMessage(ChatColor.AQUA + "술래가 움직이면 소리를 내는 플러그인입니다.");
					player.sendMessage(ChatColor.AQUA + "술래는 쉬프트를 누르며 움직이지 않는 이상, 움직일때 방울 소리가 납니다.");
					player.sendMessage(ChatColor.AQUA + "이것 또한 제 다른 플러그인이 그렇듯이 심심해서 만들었습니다.");
					player.sendMessage(ChatColor.DARK_GRAY + "By. 오그리");
				}
				else {
					this.logger.info("[방울 술래잡기]");
					this.logger.info("술래가 움직이면 소리를 내는 플러그인입니다.");
					this.logger.info("술래는 쉬프트를 누르며 움직이지 않는 이상, 움직일때 방울 소리가 납니다.");
					this.logger.info("심심해서 만들었습니다.");
					this.logger.info("By. 오그리");
				}
				return true;
			} else if(args[0].equalsIgnoreCase("add")) {// belltag add <player>
				if(player != null && !player.isOp()) {//player not op.
					player.sendMessage(ChatColor.RED + "OP도 아닌 주제에!");
					return true;
				} else if(args.length < 2) { //player not given.
					if(player != null) {
						player.sendMessage(ChatColor.RED + "술래 목록에 추가할 플레이어를 입력해야 한다.");
					}
					else this.logger.warning("술래 목록에 추가할 플레이어를 입력해라.");
					return true;
				} else {//belltag add[0] <pl1>[1] <pl2>[2] ...
					List<String> invalidPlayers = new ArrayList<String>();
					//player.sendMessage(args);
					//player.sendMessage("" + args.length);
					for(int i=1 ; i<args.length ; i++) {
						boolean isOnline = false;
						//player.sendMessage(i + ": " + args[i]);
						for(Player p: Bukkit.getOnlinePlayers()) {//check if the player is online
							if(p.getName().equals(args[i])) {
								isOnline = true;
								if(TagMethods.isTagger(p.getName())) invalidPlayers.add(p.getName());
								else taggers.add(p.getName());
							}
						}
						if(!isOnline) invalidPlayers.add(args[i]);
					}
					int cnt = 1;
					if(player != null) { //술래 등록 끝.
						player.sendMessage(ChatColor.YELLOW + "---------등록된 술래---------");
						for(String s: taggers) {
							player.sendMessage(ChatColor.DARK_GRAY + "" + (cnt++) + ". "
									+ ChatColor.GOLD + s);
						}
						player.sendMessage(ChatColor.YELLOW + "---------------------------");

						if(!invalidPlayers.isEmpty()) {
							String str = ChatColor.RED + "다음 플레이어들이 정상 등록되지 않았습니다: ";
							for(String s: invalidPlayers) {
								str += s + " ";
							}
							player.sendMessage(str);
						}
					}
					else {
						this.logger.info("---------등록된 술래---------");
						for(String s: taggers) {
							this.logger.info(ChatColor.DARK_GRAY + "" + (cnt++) + ". "
                                    + ChatColor.GOLD + s);
						}
						this.logger.info(ChatColor.YELLOW + "---------------------------");

						if(!invalidPlayers.isEmpty()) {
							String str = ChatColor.RED + "다음 플레이어들이 정상 등록되지 않았습니다: ";
							for(String s: invalidPlayers) {
								str += s + " ";
							}
							this.logger.info(str);
						}//하 내가 이렇게 친절한 개발자였었다니
					}
					return true;
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("clearlist")) {
				if(player != null && !player.isOp()) {//player not op.
					player.sendMessage(ChatColor.RED + "OP도 아닌 주제에!");
					return true;
				} else {
                    taggers.clear();
					if(player != null) {
						player.sendMessage(ChatColor.BLUE + "성공적으로 술래 목록을 초기화시켰습니다.");
					}
					else {
						this.logger.info("성공적으로 술래 목록을 초기화시켰습니다.");
					}
					return true;
				}
			} else if(args[0].equalsIgnoreCase("start")){
				if(player != null && !player.isOp()) {//player not op.
					player.sendMessage(ChatColor.RED + "OP도 아닌 주제에!");
					return true;
				}
				else if(args.length == 1) { //Number not given
					if(isEnabled) { //Plugin already started.
						if(player != null) {
							player.sendMessage(ChatColor.RED + "이미 플러그인이 시작된 상태다.");
						}
						else {
							this.logger.warning("플러그인이 이미 시작된 상태다.");
						}
						return true;
					}
					else if(taggers.isEmpty()) { //No tagger given
						if(player != null) {
							player.sendMessage(ChatColor.RED + "술래를 지정한 후에 하던지, 랜덤으로 돌리던지 하란 말이다.");
						}
						else {
							this.logger.warning("술래를 먼저 지정하시고 하시던가, 랜덤으로 돌리던가 하라고.");
						}
						return true;
					}
					else { //Tagger already given
						if(player != null) {
							Bukkit.broadcastMessage(ChatColor.YELLOW
									+ "플레이어 " + ChatColor.LIGHT_PURPLE
									+ player.getName() + ChatColor.YELLOW
									+ "(이)가 " + ChatColor.DARK_PURPLE
									+ "[방울 술래잡기] " + ChatColor.YELLOW
									+ "플러그인을 시작했습니다.");
							Bukkit.broadcastMessage(ChatColor.GOLD
									+ "플러그인 설명: " + ChatColor.LIGHT_PURPLE
									+ "/belltag info");
							Bukkit.broadcastMessage(ChatColor.YELLOW + "---------등록된 술래---------");
							for(int i=0 ; i<taggers.size() ; i++) {
								Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "" + (i+1) + ". "
										+ ChatColor.GOLD + taggers.get(i));
							}
							Bukkit.broadcastMessage(ChatColor.YELLOW + "--------------------------");
							Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Made by. 감염오리");
						} else {
							Bukkit.broadcastMessage(ChatColor.YELLOW + "서버에서 "
									+ ChatColor.DARK_PURPLE + "[방울 술래잡기] "
									+ ChatColor.YELLOW + "플러그인을 시작했습니다.");
							Bukkit.broadcastMessage(ChatColor.GOLD
									+ "플러그인 설명: " + ChatColor.LIGHT_PURPLE
									+ "/belltag info");
							Bukkit.broadcastMessage(ChatColor.YELLOW + "---------등록된 술래---------");
                            for(int i=0 ; i<taggers.size() ; i++) {
                                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "" + (i+1) + ". "
                                        + ChatColor.GOLD + taggers.get(i));
                            }
							Bukkit.broadcastMessage(ChatColor.YELLOW + "--------------------------");
							Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Made by. 감염오리");
						}
                        playerBellCount.clear();
						for(Player pl: Bukkit.getOnlinePlayers()) {
                            for(String s: taggers) {
                                if(s.equalsIgnoreCase(pl.getName())) {
                                    TagMethods.giveTaggerItems(pl);
                                    break;
                                }
                            }
							playerBellCount.put(pl.getName(), 0);
						}
						if(invisibleNametag) TagMethods.setTeams();
						if(winCounter) TagScheduler.sidebarCountDownWithSound(timeToWin);
						if(markerEnabled) TagScheduler.setMarkerTask(markerDelay);
						isEnabled = true;
						return true;
					}
				}
				if(args.length == 2) { //number given
					if(isEnabled) { //Plugin already started.
						if(player != null) {
							player.sendMessage(ChatColor.RED + "이미 플러그인이 시작된 상태다.");
						}
						else {
							this.logger.warning("플러그인이 이미 시작된 상태다.");
						}
						return true;
					} else if(Integer.parseInt(args[1]) <= Bukkit.getOnlinePlayers().size()
							&& 0 <= Integer.parseInt(args[1])) {

						int plNum = Integer.parseInt(args[1]);

						Random random = new Random();
						Integer[] rand = new Integer[plNum];
						for(int i=0 ; i<plNum ; i++) {
							while(true) {
								rand[i] = random.nextInt(Bukkit.getOnlinePlayers().size());
								boolean isValid = true;
								for(int j=0 ; j<i ; j++) {
									if(rand[i].equals(rand[j])) isValid = false;
								}
								if(isValid) break;
							}
						}
						taggers.clear();

                        List<Player> onlinePlayers = new ArrayList<Player>();
                        onlinePlayers.addAll(Bukkit.getOnlinePlayers());
						for(int i=0 ; i<plNum ; i++) {
							taggers.add(onlinePlayers.get(rand[i]).getName());
						}

						int cnt = 1;

						if(player != null) {
							Bukkit.broadcastMessage(ChatColor.YELLOW
									+ "플레이어 " + ChatColor.LIGHT_PURPLE
									+ player.getName() + ChatColor.YELLOW
									+ "(이)가 " + ChatColor.DARK_PURPLE
									+ "[방울 술래잡기] " + ChatColor.YELLOW
									+ "플러그인을 시작했습니다.");
							Bukkit.broadcastMessage(ChatColor.GOLD
									+ "플러그인 설명: " + ChatColor.LIGHT_PURPLE
									+ "/belltag info");
							Bukkit.broadcastMessage(ChatColor.YELLOW + "---------랜덤 술래---------");
							for(String s: taggers) {
								Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "" + (cnt++) + ". "
										+ ChatColor.GOLD + s);
							}
							Bukkit.broadcastMessage(ChatColor.YELLOW + "-------------------------");
							Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Made by. 감염오리");
						} else {
							Bukkit.broadcastMessage(ChatColor.YELLOW + "서버에서 "
									+ ChatColor.DARK_PURPLE + "[방울 술래잡기] "
									+ ChatColor.YELLOW + "플러그인을 시작했습니다.");
							Bukkit.broadcastMessage(ChatColor.GOLD
									+ "플러그인 설명: " + ChatColor.LIGHT_PURPLE
									+ "/belltag info");
							Bukkit.broadcastMessage(ChatColor.YELLOW + "---------랜덤 술래---------");
                            for(String s: taggers) {
                                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "" + (cnt++) + ". "
                                        + ChatColor.GOLD + s);
                            }
							Bukkit.broadcastMessage(ChatColor.YELLOW + "-------------------------");
							Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Made by. 감염오리");
						}

						playerBellCount.clear();
                        for(Player pl: Bukkit.getOnlinePlayers()) {
                            for(String s: taggers) {
                                if(s.equalsIgnoreCase(pl.getName())) {
                                    TagMethods.giveTaggerItems(pl);
                                    break;
                                }
                            }
                            playerBellCount.put(pl.getName(), 0);
						}

						if(invisibleNametag) TagMethods.setTeams();
						if(winCounter) TagScheduler.sidebarCountDownWithSound(timeToWin);
						if(markerEnabled) TagScheduler.setMarkerTask(markerDelay);
						isEnabled = true;

						return true;
					} else {//too big a number was given
						if(player != null)
							player.sendMessage(ChatColor.RED + "니가 입력한 숫자에 대해 생각은 해봤니?");
						else
							this.logger.info("니가 입력한 숫자에 대해 생각은 해봤니?");
						return true;
					}
				}
			} else if(args[0].equalsIgnoreCase("stop") && args.length == 1) {
				if(!isEnabled) {
					if(player != null)
						player.sendMessage(ChatColor.RED + "플러그인을 시작도 안했는데 멈추겠다고?");
					else
						this.logger.warning("플러그인 시작도 안했는데 멈추려고 하다니...");
					return true;
				}
				else {
					isEnabled = false;
					TagScheduler.cancelMarkerTask();

					if(player != null) {
						Bukkit.broadcastMessage(ChatColor.AQUA + "플레이어 "
								+ ChatColor.LIGHT_PURPLE + player.getName()
								+ ChatColor.AQUA + "이(가) "
								+ ChatColor.DARK_PURPLE + "[방울 술래잡기] "
								+ ChatColor.AQUA + "플러그인을 중지했습니다.");
						player.sendMessage(ChatColor.BLUE + "플러그인을 성공적으로 중지했습니다.");
					}
					else {
						Bukkit.broadcastMessage(ChatColor.AQUA + "터미널에서"
								+ ChatColor.DARK_PURPLE + "[방울 술래잡기] "
								+ ChatColor.AQUA + "플러그인을 중지했습니다.");
						this.logger.info("플러그인을 성공적으로 중지했습니다.");
					}
					return true;
				}
			}
		}
		return false;
	}
}
