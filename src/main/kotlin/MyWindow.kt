import business.*
import config.Config
import enums.Direction
import javafx.application.Application
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import model.*
import org.itheima.kotlin.game.core.Window
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 我的窗体
 */
class MyWindow : Window(
    "坦克大战", "img/log.jfif",
    Config.width, Config.height
) {
//    var viewList = arrayListOf<View>()
    //使用线程安全的集合
    private var viewList = CopyOnWriteArrayList<View>()
    lateinit var tank: Tank
    override fun onCreate() {
        var file = File(javaClass.getResource("/map/1.map").path)
        var row = 0;
        file.readLines().forEach() {
            var coloum = 0;
            it.toCharArray().forEach { char ->
                when (char) {
                    'W' -> {
                        viewList.add(Wall(coloum * Config.block, row * Config.block))
                    }
                    'F' -> {
                        viewList.add(Fe(coloum * Config.block, row * Config.block))
                    }
                    'G' -> {
                        viewList.add(Grass(coloum * Config.block, row * Config.block))
                    }
                    'A' -> {
                        viewList.add(Water(coloum * Config.block, row * Config.block))
                    }
                    'T' -> {
                        tank = Tank(coloum * Config.block, row * Config.block)
                        viewList.add(tank)
                    }
                    'E' -> {
                        viewList.add(Enemy(coloum * Config.block,row * Config.block))
                    }
                }
                coloum++
            }
            row++
        }
    }

    override fun onDisplay() {
        viewList.forEach() {
            it.draw()
        }
//        println(viewList.size)
    }

    override fun onKeyPressed(event: KeyEvent) {
        when (event.code) {
            KeyCode.W -> {
                tank.move(Direction.UP)
            }
            KeyCode.S -> {
                tank.move(Direction.DOWN)
            }
            KeyCode.A -> {
                tank.move(Direction.LEFT)
            }
            KeyCode.D -> {
                tank.move(Direction.RIGHT)
            }
            KeyCode.SPACE -> {
                var shot = tank.shot()
                shot?.let { viewList.add(shot) }

            }
        }
    }

    override fun onRefresh() {
        viewList.filterIsInstance<MoveAble>().forEach(){ move ->
            var badDirection :Direction? = null
            var badBlock :BlockAble? = null

            viewList.filterIsInstance<BlockAble>().forEach(){block->
                if(move == block) return@forEach
               //move 和block 是否碰撞
                var direction = move.willCollision(block)
                direction?.let {
                    badDirection = direction
                    badBlock = block
                    return@forEach
                }
            }
            move.notifyCollison(badDirection,badBlock)
        }
        viewList.filterIsInstance<FlyAble>().forEach(){
            it.autoMove()
        }
        viewList.filterIsInstance<DestroyAble>().forEach(){
            if(it.destroy()){
                viewList.remove(it)
            }
        }
        viewList.filterIsInstance<AttackAble>().forEach(){ attack ->
            attack as AttackAble
            //过滤

//            viewList.filterIsInstance<SufferAble>().forEach(){ suffer ->
            viewList.filter { (it is SufferAble ) and (attack.owner !=it) }.forEach(){ suffer ->
                suffer as SufferAble
                if(attack.isConllision(suffer)){
                    //notice attack isCollision
                    attack.notifyAttack(suffer)
                    //notice suffer isCollision
                    var sufferView = suffer.notifyAttack(attack)
                    sufferView?.let {
                        //显示爆炸物
                        viewList.addAll(sufferView)
                    }

                    return@forEach
                }
            }
        }
        viewList.filterIsInstance<AutoShotAble>().forEach(){ autoShot ->
            autoShot as AutoShotAble
            val shot = autoShot.autoShot()
            shot?.let {
                viewList.add(shot)
            }
        }


    }
}

fun main() {
    Application.launch(MyWindow::class.java)
}