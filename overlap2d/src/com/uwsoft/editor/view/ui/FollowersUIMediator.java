/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package com.uwsoft.editor.view.ui;

import com.badlogic.ashley.core.Entity;
import com.commons.MsgAPI;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.BaseNotification;
import com.puremvc.patterns.observer.Notification;
import com.uwsoft.editor.Overlap2DFacade;
import com.uwsoft.editor.controller.commands.CompositeCameraChangeCommand;
import com.uwsoft.editor.controller.commands.ConvertToCompositeCommand;
import com.uwsoft.editor.renderer.components.NodeComponent;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;
import com.uwsoft.editor.view.stage.Sandbox;
import com.uwsoft.editor.view.stage.SandboxMediator;
import com.uwsoft.editor.view.stage.tools.PanTool;
import com.uwsoft.editor.view.ui.box.UIToolBoxMediator;
import com.uwsoft.editor.view.ui.followers.BasicFollower;
import com.uwsoft.editor.view.ui.followers.FollowerFactory;
import com.uwsoft.editor.view.ui.followers.NormalSelectionFollower;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by azakhary on 5/20/2015.
 */
public class FollowersUIMediator extends SimpleMediator<FollowersUI> {
    private static final String TAG = FollowersUIMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private HashMap<Entity, BasicFollower> followers = new HashMap<>();

    public FollowersUIMediator() {
        super(NAME, new FollowersUI());
    }

    @Override
    public void onRegister() {
        facade = Overlap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.SCENE_LOADED,
                MsgAPI.ITEM_DATA_UPDATED,
                MsgAPI.ITEM_SELECTION_CHANGED,
                MsgAPI.SHOW_SELECTIONS,
                MsgAPI.HIDE_SELECTIONS,
                MsgAPI.NEW_ITEM_ADDED,
                PanTool.SCENE_PANNED,
                UIToolBoxMediator.TOOL_SELECTED,
                MsgAPI.ITEM_PROPERTY_DATA_FINISHED_MODIFYING,
                CompositeCameraChangeCommand.DONE,
                MsgAPI.ZOOM_CHANGED,
                ConvertToCompositeCommand.DONE
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case CompositeCameraChangeCommand.DONE:
                createFollowersForAllVisible();
            case MsgAPI.SCENE_LOADED:
                createFollowersForAllVisible();
                break;
            case MsgAPI.NEW_ITEM_ADDED:
                createFollower(notification.getBody());
                break;
            case MsgAPI.ITEM_PROPERTY_DATA_FINISHED_MODIFYING:
                BasicFollower follower = followers.get(notification.getBody());
                if(follower != null) {
                    follower.update();
                }
                break;
            case MsgAPI.ITEM_DATA_UPDATED:
                follower = followers.get(notification.getBody());
                if(follower != null) {
                    follower.update();
                }
                break;
            case PanTool.SCENE_PANNED:
                updateAllFollowers();
                break;
            case MsgAPI.ITEM_SELECTION_CHANGED:
                clearAllSubFollowersExceptNew(notification.getBody());
                setNewSelectionConfiguration(notification.getBody());
                break;
            case MsgAPI.HIDE_SELECTIONS:
                hideAllFollowers(notification.getBody());
                break;
            case MsgAPI.SHOW_SELECTIONS:
                showAllFollowers(notification.getBody());
                break;
            case UIToolBoxMediator.TOOL_SELECTED:
                pushNotificationToFollowers(notification);
                break;
            case MsgAPI.ZOOM_CHANGED:
                updateAllFollowers();
                break;
            case ConvertToCompositeCommand.DONE:
                // because entities changed their parent, it's better to re-make all followers
                removeAllfollowers();
                createFollowersForAllVisible();
                break;
        }
    }

    public void pushNotificationToFollowers(Notification notification) {
        for (BasicFollower follower : followers.values()) {
            follower.handleNotification(notification);
        }
    }

    private void clearAllSubFollowersExceptNew(Set<Entity> items) {
        for (BasicFollower follower : followers.values()) {
            if(!items.contains(follower)) {
                if(follower instanceof NormalSelectionFollower) {
                    ((NormalSelectionFollower)follower).clearSubFollowers();
                }
            }
        }
    }

    private void setNewSelectionConfiguration(Set<Entity> items) {
        followers.values().forEach(com.uwsoft.editor.view.ui.followers.BasicFollower::hide);
        for (Entity item : items) {
            followers.get(item).show();
        }
    }

    private void createFollowersForAllVisible() {
        removeAllfollowers();
        Sandbox sandbox = Sandbox.getInstance();
        NodeComponent nodeComponent = ComponentRetriever.get(sandbox.getCurrentViewingEntity(), NodeComponent.class);

        for (Entity entity: nodeComponent.children) {
            createFollower(entity);
        }
//        java.lang.NullPointerException
//        at com.uwsoft.editor.view.ui.FollowersUIMediator.createFollowersForAllVisible(FollowersUIMediator.java:157)
//        at com.uwsoft.editor.view.ui.FollowersUIMediator.handleNotification(FollowersUIMediator.java:83)
//        at com.puremvc.patterns.observer.BaseObserver.notifyObserver(BaseObserver.java:82)
//        at com.puremvc.core.CoreView.notifyObservers(CoreView.java:128)
//        at com.puremvc.patterns.facade.SimpleFacade.notifyObservers(SimpleFacade.java:361)
//        at com.puremvc.patterns.facade.SimpleFacade.sendNotification(SimpleFacade.java:323)
//        at com.puremvc.patterns.facade.SimpleFacade.sendNotification(SimpleFacade.java:337)
//        at com.uwsoft.editor.controller.commands.CompositeCameraChangeCommand.doAction(CompositeCameraChangeCommand.java:63)
//        at com.uwsoft.editor.controller.commands.RevertableCommand.callDoAction(RevertableCommand.java:49)
//        at com.uwsoft.editor.controller.commands.RevertableCommand.execute(RevertableCommand.java:40)
//        at com.puremvc.core.CoreController.executeCommand(CoreController.java:139)
//        at com.puremvc.patterns.observer.BaseObserver.notifyObserver(BaseObserver.java:82)
//        at com.puremvc.core.CoreView.notifyObservers(CoreView.java:128)
//        at com.puremvc.patterns.facade.SimpleFacade.notifyObservers(SimpleFacade.java:361)
//        at com.puremvc.patterns.facade.SimpleFacade.sendNotification(SimpleFacade.java:323)
//        at com.puremvc.patterns.facade.SimpleFacade.sendNotification(SimpleFacade.java:337)
//        at com.uwsoft.editor.view.stage.tools.SelectionTool.itemMouseDoubleClick(SelectionTool.java:330)
//        at com.uwsoft.editor.view.stage.SandboxMediator$SandboxItemEventListener.touchUp(SandboxMediator.java:231)
//        at com.uwsoft.editor.view.stage.input.SandboxInputAdapter.touchUp(SandboxInputAdapter.java:138)
//        at com.badlogic.gdx.InputMultiplexer.touchUp(InputMultiplexer.java:96)
//        at com.badlogic.gdx.backends.lwjgl.LwjglInput.processEvents(LwjglInput.java:316)
//        at com.badlogic.gdx.backends.lwjgl.LwjglCanvas$3.run(LwjglCanvas.java:234)
//        at java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:311)
//        at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:756)
//        at java.awt.EventQueue.access$500(EventQueue.java:97)
//        at java.awt.EventQueue$3.run(EventQueue.java:709)
//        at java.awt.EventQueue$3.run(EventQueue.java:703)
//        at java.security.AccessController.doPrivileged(Native Method)
//        at java.security.ProtectionDomain$JavaSecurityAccessImpl.doIntersectionPrivilege(ProtectionDomain.java:76)
//        at java.awt.EventQueue.dispatchEvent(EventQueue.java:726)
//        at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:201)
//        at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:116)
//        at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:105)
//        at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
//        at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:93)
//        at java.awt.EventDispatchThread.run(EventDispatchThread.java:82)
//        Exception in thread "AWT-EventQueue-0" java.lang.RuntimeException: No OpenGL context found in the current thread.
//        at org.lwjgl.opengl.GLContext.getCapabilities(GLContext.java:124)
//        at org.lwjgl.opengl.GL11.glDeleteTextures(GL11.java:732)
//        at com.badlogic.gdx.backends.lwjgl.LwjglGL20.glDeleteTexture(LwjglGL20.java:248)
//        at com.badlogic.gdx.graphics.GLTexture.delete(GLTexture.java:170)
//        at com.badlogic.gdx.graphics.Texture.dispose(Texture.java:194)
//        at com.badlogic.gdx.graphics.g2d.TextureAtlas.dispose(TextureAtlas.java:418)
//        at com.badlogic.gdx.scenes.scene2d.ui.Skin.dispose(Skin.java:389)
//        at com.kotcrab.vis.ui.VisUI.dispose(VisUI.java:114)
//        at com.uwsoft.editor.Overlap2D.dispose(Overlap2D.java:68)
//        at com.badlogic.gdx.backends.lwjgl.LwjglCanvas$4.run(LwjglCanvas.java:309)
//        at java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:311)
//        at java.awt.EventQueue.dispatchEventImpl(EventQueue.java:756)
//        at java.awt.EventQueue.access$500(EventQueue.java:97)
//        at java.awt.EventQueue$3.run(EventQueue.java:709)
//        at java.awt.EventQueue$3.run(EventQueue.java:703)
//        at java.security.AccessController.doPrivileged(Native Method)
//        at java.security.ProtectionDomain$JavaSecurityAccessImpl.doIntersectionPrivilege(ProtectionDomain.java:76)
//        at java.awt.EventQueue.dispatchEvent(EventQueue.java:726)
//        at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:201)
//        at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:116)
//        at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:105)
//        at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
//        at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:93)
//        at java.awt.EventDispatchThread.run(EventDispatchThread.java:82)
    }

    private void removeAllfollowers() {
        followers.values().forEach(com.uwsoft.editor.view.ui.followers.BasicFollower::remove);
        followers.clear();
    }

    private void hideAllFollowers(Set<Entity> items) {
        for (Entity item : items) {
            followers.get(item).hide();
        }
    }

    private void showAllFollowers(Set<Entity> items) {
        for (Entity item : items) {
            followers.get(item).show();
        }
    }

    private void updateAllFollowers() {
        followers.values().forEach(com.uwsoft.editor.view.ui.followers.BasicFollower::update);
    }

    public void createFollower(Entity entity) {
        BasicFollower follower = FollowerFactory.createFollower(entity);
        viewComponent.addActor(follower);
        followers.put(entity, follower);

        SandboxMediator sandboxMediator = facade.retrieveMediator(SandboxMediator.NAME);
        follower.handleNotification(new BaseNotification(UIToolBoxMediator.TOOL_SELECTED, sandboxMediator.getCurrentSelectedToolName()));
    }

    public void removeFollower(Entity entity) {
        followers.get(entity).remove();
        followers.remove(entity);
    }

    public void clearAllListeners() {
        followers.values().forEach(com.uwsoft.editor.view.ui.followers.BasicFollower::clearFollowerListener);
    }

    public BasicFollower getFollower(Entity entity) {
        return followers.get(entity);
    }
}
