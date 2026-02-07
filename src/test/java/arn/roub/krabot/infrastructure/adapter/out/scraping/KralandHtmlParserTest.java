package arn.roub.krabot.infrastructure.adapter.out.scraping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KralandHtmlParserTest {

    private KralandHtmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new KralandHtmlParser();
    }

    @Test
    void hasNotification_whenBadgeContainsExclamation_returnsTrue() {
        String html = """
                <ul class="nav navbar-nav navbar-right">
                    <li><a href="" onclick="javascript:openReport();return false;"><i class="fa fa-bell"></i><span class="badge badge-danger">!</span></a></li>
                </ul>
                """;

        assertTrue(parser.hasNotification(html));
    }

    @Test
    void hasNotification_whenBadgeIsEmpty_returnsFalse() {
        String html = """
                <ul class="nav navbar-nav navbar-right">
                    <li><a href="" onclick="javascript:openReport();return false;"><i class="fa fa-bell"></i><span class="badge badge-danger"></span></a></li>
                </ul>
                """;

        assertFalse(parser.hasNotification(html));
    }

    @Test
    void hasNotification_whenNoBadge_returnsFalse() {
        String html = """
                <ul class="nav navbar-nav navbar-right">
                    <li><a href="" onclick="javascript:openReport();return false;"><i class="fa fa-bell"></i></a></li>
                </ul>
                """;

        assertFalse(parser.hasNotification(html));
    }

    @Test
    void hasNotification_whenNoBellIcon_returnsFalse() {
        String html = """
                <ul class="nav navbar-nav navbar-right">
                    <li><a href="#"><span class="badge badge-danger">!</span></a></li>
                </ul>
                """;

        assertFalse(parser.hasNotification(html));
    }

    @Test
    void hasNotification_whenBadgeContainsOtherText_returnsFalse() {
        String html = """
                <ul class="nav navbar-nav navbar-right">
                    <li><a href="" onclick="javascript:openReport();return false;"><i class="fa fa-bell"></i><span class="badge badge-danger">5</span></a></li>
                </ul>
                """;

        assertFalse(parser.hasNotification(html));
    }

    @Test
    void isSleepButtonAvailable_whenButtonHasBtnPrimaryClass_returnsTrue() {
        String html = """
                <nav class="navbar navbar-inverse nomargin">
                	<div class="container">
                		<div class="navbar-header">
                			<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                				<span class="sr-only">Toggle navigation</span>
                				<span class="icon-bar"></span>
                				<span class="icon-bar"></span>
                				<span class="icon-bar"></span>
                			</button>
                			<a class="navbar-brand" href="accueil">Kraland</a>
                		</div>
                		<div id="navbar" class="navbar-collapse collapse navbar-collapse1">
                			<ul class="nav navbar-nav">
                				<li class="dropdown active">
                					<a href="jouer" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Jouer <span class="caret"></span></a>
                					<ul class="dropdown-menu" role="menu">
                						<li><a href="jouer/plateau">Plateau</a></li>
                						<li><a href="jouer/materiel">Matériel</a></li>
                						<li><a href="jouer/perso">Personnage</a></li>
                						<li><a href="jouer/bat">Bâtiments</a></li>
                						<li><a href="jouer/pnj">Employés</a></li>
                					</ul>
                				</li>
                				<li class="dropdown">
                					<a href="regles" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Règles <span class="caret"></span></a>
                					<ul class="dropdown-menu" role="menu">
                						<li><a href="regles/base">Règles de Base</a></li>
                						<li><a href="regles/avancees">Règles Avancées</a></li>
                						<li class="divider"></li>
                						<li class="dropdown-header">Vocations</li>
                						<li><a href="regles/carrieres">Carrières</a></li>
                						<li><a href="regles/types">Types</a></li>
                						<li><a href="regles/politique">Politique</a></li>
                						<li><a href="regles/combat">Combat</a></li>
                						<li><a href="regles/pouvoir">Pouvoir</a></li>
                						<li class="divider"></li>
                						<li class="dropdown-header">Gestion Publique</li>
                						<li><a href="regles/ville">Gestion de Ville</a></li>
                						<li><a href="regles/province">Gestion de Province</a></li>
                						<li><a href="regles/empire">Gestion d'Empire</a></li>
                						<li class="divider"></li>
                						<li class="dropdown-header">Annexes</li>
                						<li><a href="regles/provinces">Provinces</a></li>
                						<li><a href="regles/materiel">Liste du Matériel</a></li>
                						<li><a href="regles/batiments">Liste des Bâtiments</a></li>
                						<li><a href="regles/bestiaire">Le Bestiaire</a></li>
                						<li><a href="regles/ordres">Liste des Ordres</a></li>
                					</ul>
                				</li>
                				<li class="dropdown">
                					<a href="monde" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Monde <span class="caret"></span></a>
                					<ul class="dropdown-menu" role="menu">
                						<li><a href="monde/citoyens">Citoyens</a></li>
                						<li><a href="monde/empires">Empires</a></li>
                						<li><a href="monde/organisations">Organisations</a></li>
                						<li><a href="monde/evenements">Événements</a></li>
                						<li><a href="monde/guide">Guide du Monde</a></li>
                					</ul>
                				</li>
                				<li class="dropdown">
                					<a href="forum" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Forum <span class="caret"></span></a>
                					<ul class="dropdown-menu" role="menu">
                						<li><a href="forum/rp">Jeu (RP)</a></li>
                						<li><a href="forum/hrp">Jeu (HRP)</a></li>
                						<li><a href="forum/communaute">Communauté</a></li>
                						<li><a href="forum/debats">Débats</a></li>
                						<li class="divider"></li>
                						<li><a href="forum/top20">Top 20</a></li>
                					</ul>
                				</li>
                				<li class="dropdown">
                					<a href="communaute" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Communauté <span class="caret"></span></a>
                					<ul class="dropdown-menu" role="menu">
                						<li><a href="communaute/membres">Membres</a></li>
                						<li><a href="communaute/chat">Mini-Chat</a></li>
                						<li><a href="communaute/soutien">Soutenir le Site</a></li>
                						<li class="divider"></li>
                						<li class="dropdown-header">Mini-Jeux</li>
                						<li><a href="communaute/civikra">Civikra</a></li>
                						<li><a href="communaute/krasolo">Kraland Solo</a></li>
                					</ul>
                				</li>
                			</ul>
                			<ul class="nav navbar-nav navbar-right">
                				<li><a href="" id="kr-toggle-css-btn" title="Activer la surcharge CSS"><i class="fa fa-palette"></i></a></li><li><a href="" onclick="javascript:openMap();return false;"><i class="fa fa-globe"></i></a></li>
                				<li><a href="" onclick="javascript:openReport();return false;"><i class="fa fa-bell"></i><span class="badge badge-danger"></span></a></li>
                				<li><a href="kramail"><i class="fa fa-envelope"></i><span class="badge" id="badge"></span></a></li>
                				<li class="dropdown">
                					<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false"><i class="fa fa-user"></i><span class="caret"></span></a>
                					<ul class="dropdown-menu" role="menu">
                						<form method="post" id="reconnect" action="reconnect">
                							<input type="hidden" name="t" value="5Gm75uVZ9znKoqGGJhend3YNc5pbDurfHIuWPF3aROE=">
                						</form>
                						<form method="post" id="exit" action="exit">
                							<input type="hidden" name="t" value="Q5MNHDd6ONjamJDyJYhN+qpRLemjvvI4e3SdSIPV9DI=">
                						</form>
                						<li><a href="communaute/membres/edit"><i class="fa fa-user"></i> Modifier profil</a></li>
                						<li><a href="profil/interface"><i class="fa fa-cog"></i> Paramètres</a></li>
                						<li><a href="contacts"><i class="fa fa-users"></i> Contacts</a></li>
                						<li><a href="profil/notes"><i class="fas fa-file-signature"></i> Notes</a></li>
                						<li><a href="aide"><i class="fa fa-medkit"></i> A l'aide !</a></li>
                						<li class="divider"></li>
                						<li><a href="javascript:document.getElementById('reconnect').submit();"><i class="fa fa-sync-alt"></i> Reconnexion</a></li>
                						<li><a href="javascript:document.getElementById('exit').submit();"><i class="fa fa-sign-out-alt"></i> Déconnexion</a></li>
                					</ul>
                				</li>
                			</ul>
                		</div>
                	</div>
                </nav>
                <div id="content" class="container">
                	<div class="row">
                		<div id="col-left" class="col-md-3 sidebar">
                			<!-- submenu -->
                			<div class="mini-submenu navbar-default">
                				<span class="icon-bar"></span>
                				<span class="icon-bar"></span>
                				<span class="icon-bar"></span>
                			</div>
                			<div class="list-group">
                				<span class="list-group-item active">
                					Thanaël le Joyeux
                					<span class="pull-right" id="slide-submenu">
                		  				<i class="fa fa-times"></i>
                					</span>
                				</span>
                				<div class="panel panel-body">
                					<div>
                						<div class="col-xs-12 col-sm-4 nopadding">
                				<a class="btn btn-default alert100 nopadding" style="border:0" href="#"><div style="width:79px;height:79px;"><img src="https://s.azup.fr/s/120-cropx20y56w450h450/2025-12-14_15-15-03-obsidianAnise_obsidianAniseV10-1374223878f05.md.png" alt="" class="center-block img-circle img-thumbnail img-responsive img-avatar"></div></a>
                						</div>
                						<div class="col-xs-12 col-sm-8 nopadding-right">
                							<div class="row center" style="margin-top:1px;">
                				<a class="btn btn-default alert107 mini" href="#" style="padding:2px 5px;"><i class="fa fa-star"></i></a>
                				<a class="btn btn-default alert101 mini" href="#" style="padding:0px;margin:0px -2px;"><img src="http://img7.kraland.org/2/voc/1/11.png"></a>
                				<a class="btn btn-default alert102 mini" href="#" style="padding:0px;margin:0px -2px;"><img src="http://img7.kraland.org/2/voc/2/0.png"></a>
                				<a class="btn btn-default alert103 mini" href="#" style="padding:0px;margin:0px -2px;"><img src="http://img7.kraland.org/2/voc/3/0.png"></a>
                				<a class="btn btn-default alert104 mini" href="#" style="padding:0px;margin:0px -2px;"><img src="http://img7.kraland.org/2/voc/4/0.png"></a>
                				<a class="btn btn-default alert105 mini" href="#" style="padding:0px;margin:0px -2px;"><img src="http://img7.kraland.org/2/voc/5/0.png"></a>
                							</div>
                							<div class="row center" style="margin-top:5px;">
                											<a class="btn btn-default alert106 mini" href="#"><img src="http://img7.kraland.org/2/world/f2.png"> </a>
                <div class="mini t"><i class="fa fa-coins"></i> 104 MØ</div>							</div>
                							<div class="row"><br>
                							</div>
                						</div>
                					</div>
                					<div class="btn-group-xs center">
                						<a class="btn btn-default alert121 mini" href="#"><i class=""></i> FOR<br><strong>1</strong></a>
                						<a class="btn btn-default alert122 mini" href="#"><i class=""></i> VOL<br><strong>2</strong></a>
                						<a class="btn btn-default alert123 mini" href="#"><i class=""></i> CHA<br><strong>1</strong></a>
                						<a class="btn btn-default alert124 mini" href="#"><i class=""></i> GES<br><strong>1</strong></a>
                						<a class="btn btn-default alert125 mini" href="#"><i class=""></i> INT<br><strong>1</strong></a>
                						<a class="btn btn-default alert126 mini" href="#"><i class=""></i> PER<br><strong>1</strong></a>
                					</div>
                					<br>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><i class="fa fa-sun"></i> Compétences</h3>
                							<span class="pull-right clickable panel-collapsed"><i class="glyphicon glyphicon-chevron-down"></i></span>
                						</div>
                						<div class="panel-body" style="display:none;">
                					<a class="list-group-item ds_game alert111" href="#" style="padding:5px">
                						<span class="mention pull-right">1</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Baratin</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert112" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Combat Mains Nues</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert113" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Combat Contact</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert114" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Combat Distance</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert115" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Commerce</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert116" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Démolition</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert117" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Discrétion</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert118" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Éloquence</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert119" href="#" style="padding:5px">
                						<span class="mention pull-right">1</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Falsification</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert1110" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Foi</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert1111" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Informatique</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert1112" href="#" style="padding:5px">
                						<span class="mention pull-right">1</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Médecine</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert1113" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Observation</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert1114" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Organisation</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert1115" href="#" style="padding:5px">
                						<span class="mention pull-right">2</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Pouvoir</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert1116" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Séduction</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert1117" href="#" style="padding:5px">
                						<span class="mention pull-right">1</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Survie</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                					<a class="list-group-item ds_game alert1118" href="#" style="padding:5px">
                						<span class="mention pull-right">0</span>
                						<h4 class="list-group-item-heading nomargin"><span class="mini">Vol</span></h4>
                						<p class="list-group-item-text"></p>
                					</a>
                						</div>
                					</div>
                					<div class="t row" style="padding-left:15px">
                						<div class="col-xs-12 col-sm-4 nopadding">
                							<div class="c100 p12 small">
                								<span>02:53</span>
                								<div class="slice">
                									<div class="bar"></div>
                									<div class="fill"></div>
                								</div>
                							</div>
                						</div>
                						<div class="col-xs-12 col-sm-8 nopadding-right">
                							<div class="row">
                								<div class="col-xs-12 col-sm-4">
                									<div class="btn-group-xs">
                										<a class="btn btn-default alert141 mini" href="#"><i class=""></i> PV&nbsp; 26</a>
                									</div>
                								</div>
                								<div class="col-xs-12 col-sm-8">
                									<div class="progress" style="height:10px;margin-top:5px">
                										<div class="progress-bar progress-bar-success" style="width: 100%"><span class="sr-only">26</span></div>
                										<div class="progress-bar progress-bar-warning" style="width: 0%"><span class="sr-only">0</span></div>
                									</div>
                								</div>
                							</div>
                							<div class="row">
                								<div class="col-xs-12 col-sm-4">
                									<div class="btn-group-xs">
                										<a class="btn btn-default alert142 mini" href="#"><i class=""></i> PM&nbsp; 25</a>
                									</div>
                								</div>
                								<div class="col-xs-12 col-sm-8">
                									<div class="progress" style="height:10px;margin-top:5px">
                										<div class="progress-bar progress-bar-success" style="width: 100%"><span class="sr-only">25</span></div>
                										<div class="progress-bar progress-bar-warning" style="width: 0%"><span class="sr-only">0</span></div>
                									</div>
                								</div>
                								<div class="col-xs-12 col-sm-4">
                				<div class="btn-group-xs">
                					<a class="btn btn-default alert143 mini" href="#"><i class=""></i> PP&nbsp; 0</a>
                				</div>
                								</div>
                								<div class="col-xs-12 col-sm-8">
                									<div class="progress" style="height:10px;margin-top:5px">
                										<div class="progress-bar progress-bar-success" style="width: 0%"><span class="sr-only">0</span></div>
                										<div class="progress-bar progress-bar-warning" style="width: 100%"><span class="sr-only">150</span></div>
                									</div>
                								</div>
                							</div>
                						</div>
                					</div>
                
                <div class="t">
                <a class="btn btn-primary alert11 mini" href="#"><i class="fa fa-bed"></i> Dormir</a>
                <a class="btn btn-primary alert12 mini" href="#"><i class="fa fa-pray"></i> Prier</a>
                <a class="btn btn-default alert14 mini" href="#"><i class="fa fa-star-half-alt"></i> Pouvoir</a>
                <hr style="border-top: 1px solid #337ab7; background: transparent;">
                <a class="btn btn-primary alert1103 mini" href="#"><i class="fab fa-get-pocket"></i> Fouiller</a>
                </div>
                				</div>
                			</div>
                			<!-- /submenu -->
                
                		</div>
                		<div id="col-right" class="col-md-9">
                				<div class="container-fluid">
                				<div class="row center">
                			<map name="exitmap">
                			<area shape="rect" coords="31,8,59,29" href="jouer/plateau/200003-1-34-5956839658" alt="Nord">
                			<area shape="rect" coords="0,31,28,52" href="jouer/plateau/200003-3-34-2956839657" alt="Sud">
                			<area shape="rect" coords="0,8,28,29" href="jouer/plateau/200003-4-34-5956839655" alt="Ouest">
                			</map>
                			<img src="http://img7.kraland.org/2/bat/exit134.gif" width="60" height="60" alt="Sortir" border="0" usemap="#exitmap" style="vertical-align:bottom">
                			<a href="jouer/plateau/200002-2810-34-4956839653" class="btn btn-primary" style="padding:1px;"><img src="http://img7.kraland.org/2/bat/bat0.gif" width="60" height="60"></a>
                			<a href="jouer/plateau" class="btn btn-warning" style="padding:1px;"><img src="http://img7.kraland.org/2/bat/bat1.gif" width="60" height="60"></a>
                			<a href="jouer/plateau/200002-3267-34-1956839651" class="btn btn-primary" style="padding:1px;"><img src="http://img7.kraland.org/2/bat/bat3.gif" width="60" height="60"></a>
                				</div>
                				<div class="row t">
                					<div class="col-md-6 col-xs-12">
                					<div class="dashboard">
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32182020" href="#" "=""><i class="fa fa-user-friends"></i></a> <a class="btn btn-default btn-xs alert34" href="#"><i class="fa fa-car-side"></i></a> Groupe Thanaël le Joyeux</h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3182020" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="https://s.azup.fr/s/120-cropx20y56w450h450/2025-12-14_15-15-03-obsidianAnise_obsidianAniseV10-1374223878f05.md.png" width="32" height="32" class="pull-left" alt="Thanaël le Joyeux">
                								<h4 class="list-group-item-heading nomargin">Thanaël le Joyeux</h4>
                								<p class="list-group-item-text">[Déprimé]</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/thanaAl-le-joyeux-2-82020" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/thanaAl-le-joyeux-10-282020" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32182221" href="#" "=""><i class="fa fa-user-friends"></i></a> Groupe Megakra</h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3182221" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f9.png" alt="9"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="https://zupimages.net/up/26/01/sdrl.jpg" width="32" height="32" class="pull-left" alt="Megakra">
                								<h4 class="list-group-item-heading nomargin">Megakra</h4>
                								<p class="list-group-item-text">&nbsp;</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/megakra-2-82221" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/megakra-10-282221" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32182209" href="#" "=""><i class="fa fa-user-friends"></i></a> Groupe Yaj Kao <img src="http://img7.kraland.org/s/4C.gif" alt="[*r]" class="icon"></h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3182209" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="http://img7.kraland.org/a/a12.png" width="32" height="32" class="pull-left" alt="Yaj Kao">
                								<h4 class="list-group-item-heading nomargin">Yaj Kao <img src="http://img7.kraland.org/s/4C.gif" alt="[*r]" class="icon"></h4>
                								<p class="list-group-item-text">&nbsp;</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/yaj-kao-2-82209" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/yaj-kao-10-282209" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32181998" href="#" "=""><i class="fa fa-user-friends"></i></a> Groupe Akhitophel</h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3181998" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="https://i.pinimg.com/736x/5e/da/e0/5edae027884c7ae2121cb19e620d0e64.jpg" width="32" height="32" class="pull-left" alt="Akhitophel">
                								<h4 class="list-group-item-heading nomargin">Akhitophel</h4>
                								<p class="list-group-item-text">&nbsp;</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/akhitophel-2-81998" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/akhitophel-10-281998" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32181969" href="#" "=""><i class="fa fa-user-friends"></i></a> Groupe Toutânkhàmoi</h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3181969" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="https://i20.servimg.com/u/f20/18/89/19/51/avatar11.jpg" width="32" height="32" class="pull-left" alt="Toutânkhàmoi">
                								<h4 class="list-group-item-heading nomargin">Toutânkhàmoi</h4>
                								<p class="list-group-item-text">&nbsp;</p>
                							</a>
                							<a class="list-group-item ds_game alert3215" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="http://img7.kraland.org/2/npc/125.jpg" width="32" height="32" class="pull-left" alt="Esclave">
                								<h4 class="list-group-item-heading nomargin">Esclave <button type="button" class="btn btn-danger btn-xs xmini">PNJ</button> </h4>
                								<p class="list-group-item-text">&nbsp;</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/toutAnkhAmoi-2-81969" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/toutAnkhAmoi-10-281969" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							<div style="height:59px;">
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32181960" href="#" "=""><i class="fa fa-user-friends"></i></a> Groupe Zi Eun</h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3181960" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="https://i.ibb.co/hFKtT1V4/Zi-EUn.png" width="32" height="32" class="pull-left" alt="Zi Eun">
                								<h4 class="list-group-item-heading nomargin">Zi Eun</h4>
                								<p class="list-group-item-text">[Amoureuse]</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/zi-eun-2-81960" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/zi-eun-10-281960" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32181958" href="#" "=""><i class="fa fa-user-friends"></i></a> Groupe Maître Corbeau</h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3181958" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="https://www.aht.li/3952101/Avatar_5_carre_-_140.png" width="32" height="32" class="pull-left" alt="Maître Corbeau">
                								<h4 class="list-group-item-heading nomargin">Maître Corbeau</h4>
                								<p class="list-group-item-text">&nbsp;</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/maAtre-corbeau-2-81958" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/maAtre-corbeau-10-281958" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32181952" href="#" "=""><i class="fa fa-user-friends"></i></a> Groupe Edouard Gaine</h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3181952" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="https://i.ibb.co/qYxQ1jfk/dreddy003.jpg" width="32" height="32" class="pull-left" alt="Edouard Gaine">
                								<h4 class="list-group-item-heading nomargin">Edouard Gaine</h4>
                								<p class="list-group-item-text">&nbsp;</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/edouard-gaine-2-81952" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/edouard-gaine-10-281952" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32181912" href="#" "=""><i class="fa fa-user-friends"></i></a> Groupe Syphilys</h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3181912" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="https://azup.fr/avatar_syph53e.gif" width="32" height="32" class="pull-left" alt="Syphilys">
                								<h4 class="list-group-item-heading nomargin">Syphilys</h4>
                								<p class="list-group-item-text">&nbsp;</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/syphilys-2-81912" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/syphilys-10-281912" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32181910" href="#" "=""><i class="fa fa-user-friends"></i></a> Groupe Sinzicula</h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3181910" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="https://i.ibb.co/nsGLP4rJ/Sinzillah-KIok.png" width="32" height="32" class="pull-left" alt="Sinzicula">
                								<h4 class="list-group-item-heading nomargin">Sinzicula</h4>
                								<p class="list-group-item-text">&nbsp;</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/sinzicula-2-81910" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/sinzicula-10-281910" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32181902" href="#" "=""><i class="fa fa-user-friends"></i></a> Groupe Oeil-de-Taupe</h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3181902" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="https://zupimages.net/up/26/01/dz9h.jpg" width="32" height="32" class="pull-left" alt="Oeil-de-Taupe">
                								<h4 class="list-group-item-heading nomargin">Oeil-de-Taupe</h4>
                								<p class="list-group-item-text">&nbsp;</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/oeil-de-taupe-2-81902" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/oeil-de-taupe-10-281902" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><a class="btn btn-default btn-xs alert32181893" href="#" "=""><i class="fa fa-user-friends"></i></a> Groupe Malek</h3>
                							<span class="pull-right clickable">
                							<i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<table style="border-collapse: collapse;width: 100%;"><tbody><tr><td>
                							<a class="list-group-item ds_game alert3181893" href="#">
                								<span class="mention pull-right"><div style="font-size:4px;"><img src="http://img7.kraland.org/2/world/f2.png" alt="2"><br><img src="http://img7.kraland.org/2/pdv1.png" width="20" height="8"></div></span>
                								<img src="https://azup.fr/MalekAvatar-Small598.png" width="32" height="32" class="pull-left" alt="Malek">
                								<h4 class="list-group-item-heading nomargin">Malek</h4>
                								<p class="list-group-item-text">&nbsp;</p>
                							</a>
                							</td><td style="width:16px;vertical-align:top;padding: 0px 0px 0px 4px">
                							<div style="height:59px;">
                							<div><a href="communaute/membres/malek-2-81893" target="_blank"><i class="fa fa-user f7"></i></a></div>
                							<div><a href="kramail/post/malek-10-281893" target="_blank"><i class="fa fa-envelope f7"></i></a></div>
                							</div>
                							</td></tr></tbody></table>
                						</div>
                					</div>
                					</div>
                					</div>
                					<!--/col-->
                					<div class="col-md-6 col-xs-12">
                					<div class="dashboard">
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><i class="fa fa-home"></i> Bâtiment</h3>
                							<span class="pull-right clickable"><i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                							<a class="list-group-item ds_game alert201" href="#">
                								<span class="mention pull-right"></span>
                								<img src="http://img7.kraland.org/2/map/1/4022.gif" width="32" height="32" class="pull-left" alt="Restaurant « La Léprochienne »">
                								<h4 class="list-group-item-heading nomargin">Restaurant « La Léprochienne »</h4>
                								<p class="list-group-item-text">Proprio : Syphilys</p>
                							</a>
                							<div class="row">
                								<div class="col-sm-4" style="padding-right:0px">
                										<div class="btn-group-xs t">
                											<a class="btn btn-default alert40" href="#"><i class="fa fa-home"></i></a>
                											<a class="btn btn-default alert41" href="#">PdB 150/150</a>
                										</div>
                								</div>
                								<div class="col-sm-8" style="padding-left:0px">
                									<div class="progress t" style="height:10px;margin-top:15px">
                										<div class="progress-bar progress-bar-success" style="width: 100%"><span class="sr-only">150</span></div>
                										<div class="progress-bar progress-bar-warning" style="width: 0%"><span class="sr-only">0</span></div>
                									</div>
                								</div>
                							</div>
                <div><center><img src="https://s.azup.fr/s/500-cropx0y0w500h500/logo_leprochiennec20.md.png" class="img-responsive" style="display:inline;"></center></div>						</div>
                					</div>
                					<div class="panel panel-default">
                						<div class="panel-heading">
                							<h3 class="panel-title"><i class="fa fa-hand-holding-usd"></i> Commerce</h3>
                							<span class="pull-right clickable"><i class="glyphicon glyphicon-chevron-up"></i></span>
                						</div>
                						<div class="panel-body">
                										<a class="list-group-item ds_game alert30" href="#">
                											<span class="mention pull-right"></span>
                											<img src="http://img7.kraland.org/2/mat/10/1000.gif" width="32" height="32" class="pull-left" alt="Caisse">
                											<h4 class="list-group-item-heading nomargin">Caisse</h4>
                											<p class="list-group-item-text">274 MØ - Salaire : 10 MØ/UT</p>
                										</a>
                										<div class="list-group-item ds_forum">
                										<h4 class="list-group-item-heading nomargin">Nourriture</h4>
                										</div>
                										<a class="list-group-item ds_game alert36391" href="#">
                											<span class="mention pull-right">40/2<br><span class="xmini">14/9</span></span>
                											<img src="http://img7.kraland.org/2/mat/11/1101.gif" width="32" height="32" class="pull-left" alt="Légume">
                											<h4 class="list-group-item-heading nomargin">Légume (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-square"></i> Racines de Mandragore</p>
                										</a>
                										<a class="list-group-item ds_game alert36392" href="#">
                											<span class="mention pull-right">40/2<br><span class="xmini">17/11</span></span>
                											<img src="http://img7.kraland.org/2/mat/11/1102.gif" width="32" height="32" class="pull-left" alt="Fruit">
                											<h4 class="list-group-item-heading nomargin">Fruit (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-square"></i> Fruit défendu</p>
                										</a>
                										<a class="list-group-item ds_game alert36393" href="#">
                											<span class="mention pull-right">40/2<br><span class="xmini">17/11</span></span>
                											<img src="http://img7.kraland.org/2/mat/11/1103.gif" width="32" height="32" class="pull-left" alt="Céréale">
                											<h4 class="list-group-item-heading nomargin">Céréale (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-square"></i> ChOca pics</p>
                										</a>
                										<a class="list-group-item ds_game alert36394" href="#">
                											<span class="mention pull-right">40/2<br><span class="xmini">10/6</span></span>
                											<img src="http://img7.kraland.org/2/mat/11/1106.gif" width="32" height="32" class="pull-left" alt="Poisson">
                											<h4 class="list-group-item-heading nomargin">Poisson (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-square"></i> Morue salée façon Ninoo</p>
                										</a>
                										<a class="list-group-item ds_game alert36395" href="#">
                											<span class="mention pull-right">80/5<br><span class="xmini">20/13</span></span>
                											<img src="http://img7.kraland.org/2/mat/11/1107.gif" width="32" height="32" class="pull-left" alt="Viande">
                											<h4 class="list-group-item-heading nomargin">Viande (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-square"></i> Les 3 petits cochons</p>
                										</a>
                										<a class="list-group-item ds_game alert36373" href="#">
                											<span class="mention pull-right">12/6<br><span class="xmini">25/16</span></span>
                											<img src="http://img7.kraland.org/2/mat/11/1111.gif" width="32" height="32" class="pull-left" alt="Tarte à la Crème [composants manquants]">
                											<h4 class="list-group-item-heading nomargin">Tarte à la Crème (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Cyprine et fruits déconfits</p>
                										</a>
                										<a class="list-group-item ds_game alert36374" href="#">
                											<span class="mention pull-right">10/3<br><span class="xmini">12/8</span></span>
                											<img src="http://img7.kraland.org/2/mat/11/1112.gif" width="32" height="32" class="pull-left" alt="Bonbon [composants manquants]">
                											<h4 class="list-group-item-heading nomargin">Bonbon (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Oeufs de Ténia</p>
                										</a>
                										<div class="list-group-item ds_forum">
                										<h4 class="list-group-item-heading nomargin">Repas</h4>
                										</div>
                										<a class="list-group-item ds_game alert36375" href="#">
                											<span class="mention pull-right">12/6<br><span class="xmini">15/10</span></span>
                											<img src="http://img7.kraland.org/2/mat/12/1201.gif" width="32" height="32" class="pull-left" alt="Salade [composants manquants]">
                											<h4 class="list-group-item-heading nomargin">Salade (17/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Pour les fuck'in lapins</p>
                										</a>
                										<a class="list-group-item ds_game alert36376" href="#">
                											<span class="mention pull-right">30/7<br><span class="xmini">30/20</span></span>
                											<img src="http://img7.kraland.org/2/mat/12/1202.gif" width="32" height="32" class="pull-left" alt="Repas avec Poisson [composants manquants]">
                											<h4 class="list-group-item-heading nomargin">Repas avec Poisson (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Morue du Linceul</p>
                										</a>
                										<a class="list-group-item ds_game alert36377" href="#">
                											<span class="mention pull-right">35/8<br><span class="xmini">35/23</span></span>
                											<img src="http://img7.kraland.org/2/mat/12/1203.gif" width="32" height="32" class="pull-left" alt="Repas avec Viande [niveau bâtiment insuffisant]">
                											<h4 class="list-group-item-heading nomargin">Repas avec Viande (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Carpaccio de Chair Froide</p>
                										</a>
                										<a class="list-group-item ds_game alert36378" href="#">
                											<span class="mention pull-right">50/12<br><span class="xmini">50/33</span></span>
                											<img src="http://img7.kraland.org/2/mat/12/1204.gif" width="32" height="32" class="pull-left" alt="Repas de Luxe [niveau bâtiment insuffisant]">
                											<h4 class="list-group-item-heading nomargin">Repas de Luxe (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Foie gras de noble Ruthvéne</p>
                										</a>
                										<a class="list-group-item ds_game alert36379" href="#">
                											<span class="mention pull-right">25/6<br><span class="xmini">25/16</span></span>
                											<img src="http://img7.kraland.org/2/mat/12/1205.gif" width="32" height="32" class="pull-left" alt="Petit Déjeuner [composants manquants]">
                											<h4 class="list-group-item-heading nomargin">Petit Déjeuner (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Mâchon Niarkalistanais</p>
                										</a>
                										<a class="list-group-item ds_game alert36380" href="#">
                											<span class="mention pull-right">20/4<br><span class="xmini">17/11</span></span>
                											<img src="http://img7.kraland.org/2/mat/12/1206.gif" width="32" height="32" class="pull-left" alt="Glace [composants manquants]">
                											<h4 class="list-group-item-heading nomargin">Glace (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Glace au casu marzu</p>
                										</a>
                										<div class="list-group-item ds_forum">
                										<h4 class="list-group-item-heading nomargin">Boissons</h4>
                										</div>
                										<a class="list-group-item ds_game alert36381" href="#">
                											<span class="mention pull-right">15/2<br><span class="xmini">11/7</span></span>
                											<img src="http://img7.kraland.org/2/mat/13/1301.gif" width="32" height="32" class="pull-left" alt="Jus de Fruit [composants manquants]">
                											<h4 class="list-group-item-heading nomargin">Jus de Fruit (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Smoothie Chair Fraiche</p>
                										</a>
                										<a class="list-group-item ds_game alert36382" href="#">
                											<span class="mention pull-right">15/2<br><span class="xmini">10/6</span></span>
                											<img src="http://img7.kraland.org/2/mat/13/1311.gif" width="32" height="32" class="pull-left" alt="Café [composants manquants]">
                											<h4 class="list-group-item-heading nomargin">Café (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Café Rigor Mortis</p>
                										</a>
                										<a class="list-group-item ds_game alert36383" href="#">
                											<span class="mention pull-right">15/2<br><span class="xmini">10/6</span></span>
                											<img src="http://img7.kraland.org/2/mat/13/1312.gif" width="32" height="32" class="pull-left" alt="Thé [composants manquants]">
                											<h4 class="list-group-item-heading nomargin">Thé (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Infusion Moelle Dormante</p>
                										</a>
                										<a class="list-group-item ds_game alert36384" href="#">
                											<span class="mention pull-right">15/2<br><span class="xmini">10/6</span></span>
                											<img src="http://img7.kraland.org/2/mat/13/1321.gif" width="32" height="32" class="pull-left" alt="Bière [composants manquants]">
                											<h4 class="list-group-item-heading nomargin">Bière (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Fermentation Spontanée Posthume</p>
                										</a>
                										<a class="list-group-item ds_game alert36385" href="#">
                											<span class="mention pull-right">80/5<br><span class="xmini">20/13</span></span>
                											<img src="http://img7.kraland.org/2/mat/13/1322.gif" width="32" height="32" class="pull-left" alt="Vin [composants manquants]">
                											<h4 class="list-group-item-heading nomargin">Vin (0/10)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Menstruations de Syphilys</p>
                										</a>
                										<a class="list-group-item ds_game alert36386" href="#">
                											<span class="mention pull-right">30/8<br><span class="xmini">33/22</span></span>
                											<img src="http://img7.kraland.org/2/mat/13/1382.gif" width="32" height="32" class="pull-left" alt="Tequila [niveau bâtiment insuffisant]">
                											<h4 class="list-group-item-heading nomargin">Tequila (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Cuvée Kraddock</p>
                										</a>
                										<div class="list-group-item ds_forum">
                										<h4 class="list-group-item-heading nomargin">Bons d'état / Loterie</h4>
                										</div>
                										<a class="list-group-item ds_game alert36387" href="#">
                											<span class="mention pull-right">50</span>
                											<img src="http://img7.kraland.org/2/mat/81/8101.gif" width="32" height="32" class="pull-left" alt="Billet de Loterie">
                											<h4 class="list-group-item-heading nomargin">Billet de Loterie</h4>
                											<p class="list-group-item-text"><i class="far fa-square"></i> </p>
                										</a>
                										<div class="list-group-item ds_forum">
                										<h4 class="list-group-item-heading nomargin">Services</h4>
                										</div>
                										<a class="list-group-item ds_game alert36388" href="#">
                											<span class="mention pull-right">100/10<br><span class="xmini">42</span></span>
                											<img src="http://img7.kraland.org/2/mat/91/9101.gif" width="32" height="32" class="pull-left" alt="Prostituée [niveau bâtiment insuffisant]">
                											<h4 class="list-group-item-heading nomargin">Prostituée (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Avec putes défiscalisées</p>
                										</a>
                										<a class="list-group-item ds_game alert36389" href="#">
                											<span class="mention pull-right">100/10<br><span class="xmini">42</span></span>
                											<img src="http://img7.kraland.org/2/mat/91/9102.gif" width="32" height="32" class="pull-left" alt="Prostitué [niveau bâtiment insuffisant]">
                											<h4 class="list-group-item-heading nomargin">Prostitué (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Gigolo avec un gros fémur</p>
                										</a>
                										<a class="list-group-item ds_game alert36390" href="#">
                											<span class="mention pull-right">11/5<br><span class="xmini">18</span></span>
                											<img src="http://img7.kraland.org/2/mat/91/9110.gif" width="32" height="32" class="pull-left" alt="Chambre [niveau bâtiment insuffisant]">
                											<h4 class="list-group-item-heading nomargin">Chambre (0/50)</h4>
                											<p class="list-group-item-text"><i class="far fa-minus-square"></i> Sans putes défiscalisées</p>
                										</a>
                						</div>
                					</div>
                					</div>
                					</div>
                				</div>
                				<!--/row-->
                			</div>
                			<!--/container-->
                		</div>
                		<!-- minichat -->
                		<div id="flap_closed">
                			<div id="flap">
                				<div class="container-fluid">
                					<div class="row">
                						<form id="chat">
                							<div class="panel panel-scroll panel-primary">
                								<div class="panel-heading">
                									<span class="glyphicon glyphicon-comment"></span> Mini-Chat
                									<div class="btn-group pull-right">
                										<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">
                											<span class="glyphicon glyphicon-chevron-down"></span>
                										</button>
                										<ul class="dropdown-menu slidedown">
                											<li><a href="javascript:updateAjax( 'ajax-chat', '1-1-0-0', '7956839655' );"><span class="glyphicon glyphicon-refresh"></span>Actualiser</a></li>
                											<li><a href="javascript:updateAjax( 'ajax-chat', '1-1-1-0', '1956839654' );"><span class="glyphicon glyphicon-bullhorn"></span>Parle, krabot !</a></li>
                											<li><a href="communaute/chat"><span class="glyphicon glyphicon-list-alt"></span>Archives</a></li>
                										</ul>
                									</div>
                								</div>
                								<div class="panel-body panel-body-scroll" id="panelchat">
                									<ul class="chat" id="ajax-chat">
                				<li class="left clearfix">
                					<span class="chat-img pull-left">
                						<img src="http://img.kraland.org/a/krabot.jpg" alt="" class="img-circle" width="50" height="50">
                						<br><small class="text-muted"><span class="glyphicon glyphicon-time"></span>19:48</small>
                					</span>
                					<div class="chat-body clearfix">
                						<div class="header">
                							<strong class="primary-font"><a href="communaute/membres/krabot-1-644"><img src="http://img7.kraland.org/s/4C.gif" alt="[*r]" class="icon">krabot<img src="http://img7.kraland.org/s/4C.gif" alt="[*r]" class="icon"></a></strong>
                						</div>
                						<p>Pour aller sur le forum, faut coucher ?</p>
                					</div>
                				</li>
                				<li class="right clearfix">
                					<span class="chat-img pull-right">
                						<img src="http://img7.kraland.org/a/a39.png" alt="" class="img-circle" width="50" height="50">
                						<br><small class="text-muted"><span class="glyphicon glyphicon-time"></span>19:48</small>
                					</span>
                					<div class="chat-body clearfix">
                						<div class="header">
                							<strong class="primary-font"><a href="communaute/membres/prof-stratequerre-1-10285">Prof. Stratequerre</a></strong>
                						</div>
                						<p>J'parle juste du forum !</p>
                					</div>
                				</li>
                				<li class="left clearfix">
                					<span class="chat-img pull-left">
                						<img src="https://photos.wikimapia.org/p/00/01/91/89/08_big.jpg" alt="" class="img-circle" width="50" height="50">
                						<br><small class="text-muted"><span class="glyphicon glyphicon-time"></span>19:46</small>
                					</span>
                					<div class="chat-body clearfix">
                						<div class="header">
                							<strong class="primary-font"><a href="communaute/membres/iamantaou-1-73088">Iamantaou</a></strong>
                						</div>
                						<p>Ce cri du coeur ! Mais ne t'inquiète pas noble doctorant, les province indéprendantes sont encore là ! ça s'appelle l'Australine, donc tu arrêtes de te tourner les pouces et tu fais chauffer ta matière grise pour débloquer au plutôt la tech pour déporter les gens là bas. =.=</p>
                					</div>
                				</li>
                				<li class="right clearfix">
                					<span class="chat-img pull-right">
                						<img src="http://img7.kraland.org/a/a39.png" alt="" class="img-circle" width="50" height="50">
                						<br><small class="text-muted"><span class="glyphicon glyphicon-time"></span>19:38</small>
                					</span>
                					<div class="chat-body clearfix">
                						<div class="header">
                							<strong class="primary-font"><a href="communaute/membres/prof-stratequerre-1-10285">Prof. Stratequerre</a></strong>
                						</div>
                						<p>Siouplait <img src="http://img7.kraland.org/s/64.gif" alt="[co]" class="icon"></p>
                					</div>
                				</li>
                				<li class="left clearfix">
                					<span class="chat-img pull-left">
                						<img src="http://img7.kraland.org/a/a39.png" alt="" class="img-circle" width="50" height="50">
                						<br><small class="text-muted"><span class="glyphicon glyphicon-time"></span>19:38</small>
                					</span>
                					<div class="chat-body clearfix">
                						<div class="header">
                							<strong class="primary-font"><a href="communaute/membres/prof-stratequerre-1-10285">Prof. Stratequerre</a></strong>
                						</div>
                						<p>RENDEZ-NOUS LES PI !!! <img src="http://img7.kraland.org/s/18.gif" alt="[:f]" class="icon"></p>
                					</div>
                				</li>
                				<li class="right clearfix">
                					<span class="chat-img pull-right">
                						<img src="http://img.kraland.org/a/krabot.jpg" alt="" class="img-circle" width="50" height="50">
                						<br><small class="text-muted"><span class="glyphicon glyphicon-time"></span>19:29</small>
                					</span>
                					<div class="chat-body clearfix">
                						<div class="header">
                							<strong class="primary-font"><a href="communaute/membres/krabot-1-644"><img src="http://img7.kraland.org/s/4C.gif" alt="[*r]" class="icon">krabot<img src="http://img7.kraland.org/s/4C.gif" alt="[*r]" class="icon"></a></strong>
                						</div>
                						<p>Faites de la pub pour Kraland !</p>
                					</div>
                				</li>
                				<li class="left clearfix">
                					<span class="chat-img pull-left">
                						<img src="https://i.postimg.cc/fL0MszXf/Henri-02.png" alt="" class="img-circle" width="50" height="50">
                						<br><small class="text-muted"><span class="glyphicon glyphicon-time"></span>19:02</small>
                					</span>
                					<div class="chat-body clearfix">
                						<div class="header">
                							<strong class="primary-font"><a href="communaute/membres/kidd-1-26838">Kidd</a></strong>
                						</div>
                						<p>Pour du bôjeu</p>
                					</div>
                				</li>
                				<li class="right clearfix">
                					<span class="chat-img pull-right">
                						<img src="http://img.kraland.org/a/krabot.jpg" alt="" class="img-circle" width="50" height="50">
                						<br><small class="text-muted"><span class="glyphicon glyphicon-time"></span>19:02</small>
                					</span>
                					<div class="chat-body clearfix">
                						<div class="header">
                							<strong class="primary-font"><a href="communaute/membres/krabot-1-644"><img src="http://img7.kraland.org/s/4C.gif" alt="[*r]" class="icon">krabot<img src="http://img7.kraland.org/s/4C.gif" alt="[*r]" class="icon"></a></strong>
                						</div>
                						<p>Bwahaha ! Les grands stratèges de l'Empire Brun sont vraiment des ploucs !</p>
                					</div>
                				</li>
                				<li class="left clearfix">
                					<span class="chat-img pull-left">
                						<img src="https://i.postimg.cc/fL0MszXf/Henri-02.png" alt="" class="img-circle" width="50" height="50">
                						<br><small class="text-muted"><span class="glyphicon glyphicon-time"></span>19:01</small>
                					</span>
                					<div class="chat-body clearfix">
                						<div class="header">
                							<strong class="primary-font"><a href="communaute/membres/kidd-1-26838">Kidd</a></strong>
                						</div>
                						<p>Le Warriorland a de la place pour tous les malheureux qui ne savent plus s'orienter.</p>
                					</div>
                				</li>
                				<li class="right clearfix">
                					<span class="chat-img pull-right">
                						<img src="http://img.kraland.org/a/krabot.jpg" alt="" class="img-circle" width="50" height="50">
                						<br><small class="text-muted"><span class="glyphicon glyphicon-time"></span>17:19</small>
                					</span>
                					<div class="chat-body clearfix">
                						<div class="header">
                							<strong class="primary-font"><a href="communaute/membres/krabot-1-644"><img src="http://img7.kraland.org/s/4C.gif" alt="[*r]" class="icon">krabot<img src="http://img7.kraland.org/s/4C.gif" alt="[*r]" class="icon"></a></strong>
                						</div>
                						<p>J'en ai vu du monde aujourd'hui !</p>
                					</div>
                				</li>
                				<li class="text-muted center">Texte généré à 20:04:25</li>
                									</ul>
                								</div>
                								<div class="panel-footer">
                									<div class="input-group">
                										<input type="hidden" name="name" class="form-control input-sm">
                										<input type="text" name="message" class="form-control input-sm" placeholder="Votre message">
                										<span class="input-group-btn btn-group-sm">
                												<a href="javascript:message=chat.message.value;javascript:panelchat.scrollTop=0;javascript:chat.message.value='';javascript:updateAjaxPost( 'ajax-chat', '1-1-0-0', '8956839657', message, chat.name.value );" class="btn btn-warning"><i class="fa fa-plus"></i></a>
                										</span>
                									</div>
                								</div>
                							</div>
                						</form>
                					</div>
                				</div>
                				<a href="jouer/plateau#flap" class="open" aria-hidden="true">MC</a>
                				<a href="jouer/plateau#flap_closed" class="closed" aria-hidden="true">MC</a>
                			</div>
                		</div>
                		<!-- /minichat -->
                	</div>
                </div>
                <!-- /content -->
                <!-- top-link -->
                <span id="top-link" class="affix-top">
                	<a href="#top" class="well well-sm" onclick="$('html,body').animate({scrollTop:0},'slow');return false;">
                		<i class="glyphicon glyphicon-chevron-up"></i>
                	</a>
                </span>
                <!-- /top-link -->
                <!-- footer -->
                <footer class="navbar-inverse">
                	<div class="container white">
                		<div class="footer-quote">« Il n'y a point de plus cruelle tyrannie que celle que l'on exerce à l'ombre des lois et avec les couleurs de la justice. » (Montesquieu)</div>
                		<div style="margin-top:5px;text-align:right;font-size:10px;">
                												<a href="" onclick="javascript:openHelp('');return false;" style="color:inherit;">Mentions légales</a> |
                												<a href="" onclick="javascript:openHelp('defkra');return false;" style="color:inherit;">Defkra 5</a> |
                Généré en 0.09 sec. | 05 jan 20:04		</div>
                	</div>
                </footer>
                <!-- /footer -->
                </body></html>
                """;

        assertTrue(parser.isSleepButtonAvailable(html));
    }
}
