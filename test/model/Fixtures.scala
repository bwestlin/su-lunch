package model

trait BiofoodFixtures {

  def html(mealNames: Seq[String]) =
    <div class="col-md-3 hors-menu text-center">
      <h2 id="tillmenyn">Dagens lunch Mån. 10/11</h2>
      {
        mealNames.map { name =>
          <div class="row">
            <div class="col-xs-2"></div>
            <div class="col-xs-10 text-left">{name}</div>
          </div>
        }
      }
      <p class="small"></p>
    </div>

  def defaultMealNames(nMeals: Int) = (1 to nMeals).map("Meal" + _)
}

object BiofoodFixtures extends BiofoodFixtures

trait FossilenFixtures {

  def html(mealName: String = "meal") =
    <div class="sv-text-portlet-content">
      <h2 class="h2" id="h-Menyvecka7915februari">Meny vecka 7,&nbsp;9-15 februari</h2>
      <h3 class="h3" id="h-Mandag">Måndag</h3>
      <p class="brodtext">mon-{mealName}1&nbsp;</p>
      <p class="brodtext">mon-{mealName}2</p>
      <p class="brodtext">mon-{mealName}3&nbsp;&nbsp; </p>
      <p class="brodtext">mon-{mealName}4</p>
      <h3 class="h3" id="h-Tisdag">Tisdag</h3>
      <p class="brodtext">tue-{mealName}1</p>
      <p class="brodtext">tue-{mealName}2</p>
      <p class="brodtext">tue-{mealName}3</p>
      <p class="brodtext">tue-{mealName}4</p>
      <h3 class="h3" id="h-Onsdag">Onsdag</h3>
      <p class="brodtext">wed-{mealName}1</p>
      <p class="brodtext">wed-{mealName}2</p>
      <p class="brodtext">wed-{mealName}3</p>
      <p class="brodtext">wed-{mealName}4</p>
      <h3 class="h3" id="h-Torsdag">Torsdag</h3>
      <p class="brodtext">thu-{mealName}1&nbsp;</p>
      <p class="brodtext">thu-{mealName}2&nbsp;</p>
      <p class="brodtext">thu-{mealName}3&nbsp;&nbsp; </p>
      <p class="brodtext">thu-{mealName}4&nbsp;</p>
      <h3 class="h3" id="h-Fredag">Fredag</h3>
      <p class="brodtext">fri-{mealName}1</p>
      <p class="brodtext">fri-{mealName}2</p>
      <p class="brodtext">fri-{mealName}3</p>
      <p class="brodtext">fri-{mealName}4</p>
      <h3 class="h3" id="h-Lordagsondag">Lördag, söndag</h3>
      <p class="brodtext">
        sunsat-{mealName}1 <br/><br/>
        sunsat-{mealName}2 <br/><br/>
        sunsat-{mealName}3 <br/><br/>
        sunsat-{mealName}4
      </p>
    </div>
}

object FossilenFixtures extends FossilenFixtures

trait KraftanFixtures {

  def html(mealName: String = "meal") =
    <div class="post-content no-thumbnail">
      <div class="post-info top">
        <span class="post-type-icon-wrap"><span class="post-type-icon"></span></span>
        <span class="post-date">16 november, 2014</span>
        <span class="no-caps post-autor">&nbsp;by  <a href="http://www.kraftan.nu/author/kraftan/" title="Inlägg av kraftan" rel="author">kraftan</a></span>
      </div>
      <div class="post-title-wrapper">
        <h2 class="post-title"><a href="http://www.kraftan.nu/menyer/lunchmeny-v-47-2/">Lunchmeny v.47</a></h2>
      </div>
      <div class="clear"></div>
      <div class="post-content-content">
        Måndag<br/>
        mon-{mealName}1<br/>
        **<br/>
        mon-{mealName}2<br/>
        &nbsp;<br/>
        Tisdag <br/>
        tue-{mealName}1<br/>
        **<br/>
        tue-{mealName}2<br/>
        &nbsp;<br/>
        Onsdag<br/>
        wed-{mealName}1<br/>
        **<br/>
        wed-{mealName}2<br/>
        &nbsp;<br/>
        Torsdag<br/>
        thu-{mealName}1<br/>
        **<br/>
        thu-{mealName}2<br/>
        &nbsp;<br/>
        &nbsp;Fredag<br/>
        fri-{mealName}1<br/>
        **<br/>
        fri-{mealName}2<br/>
        &nbsp;<br/>
        foo bar<br/>
        <div class="clear"></div>
        <div class="post-info bottom">
          <span class="post-type-icon-wrap"><span class="post-type-icon"></span></span>
          <span class="no-caps">in</span><a href="http://www.kraftan.nu/category/menyer/" rel="category tag">Menyer</a>
          <span class="comments-number"><a href="http://www.kraftan.nu/menyer/lunchmeny-v-47-2/#comments">0<span class="no-caps">comments</span></a></span>
        </div>
        <div class="clear"></div>
      </div>
    </div>
}

object KraftanFixtures extends KraftanFixtures

trait LantisFixtures {

  def html(mealNames: Seq[String]) =
    <div class="col-md-3 hors-menu text-center">
      <h2 id="tillmenyn">Dagens lunch Mån. 10/11</h2>
      {
        mealNames.map { name =>
          <div class="row">
            <div class="col-xs-2"></div>
            <div class="col-xs-10 text-left">{name}</div>
          </div>
        }
      }
      <p class="small"></p>
    </div>

  def defaultMealNames(nMeals: Int) = (1 to nMeals).map("Meal" + _)
}

object LantisFixtures extends LantisFixtures
