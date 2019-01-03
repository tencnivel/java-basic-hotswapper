/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tencnivel.hotswapper;

import com.sun.jdi.AbsentInformationException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import java.io.FileInputStream;
import java.nio.file.Path;
import org.apache.poi.util.IOUtils;

/**
 * 
 * Adapted from: 
 * https://www.programcreek.com/java-api-examples/?code=HotswapProjects/HotswapAgent/HotswapAgent-master/plugin/hotswap-agent-proxy-plugin/src/test/java/org/hotswap/agent/plugin/proxy/JDIRedefiner.java#
 */
public class JDIRedefiner {

    
    private Logger logger = Logger.getLogger(JDIRedefiner.class.getName());
    
    private static final String PORT_ARGUMENT_NAME = "port";
    private static final String TRANSPORT_NAME = "dt_socket";

    private VirtualMachine vm;

    public void redefineClass(Path pathToClass) {
        
        String className = pathToClass.getName(pathToClass.getNameCount() - 1).toString();
        className = className.replace(".class","");
        this.logger.log(Level.INFO, String.format("Reload class[%s]", className));
        
        Map<ReferenceType,byte[]> map = new HashMap<>();
        
        for (ReferenceType ref : vm.allClasses()){
         
            if (ref.name().endsWith(className)) {                        
                
                try {
                    map.put(ref, IOUtils.toByteArray(
                            new FileInputStream(pathToClass.toFile())
                    ));                    
                } catch (IOException ex) {
                    Logger.getLogger(JDIRedefiner.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            
        }
        
        this.redefineClasses(map);
        
    }

    
    public JDIRedefiner(int port) throws IOException {
            vm = connect(port);
    }

    public void close() throws IOException {
            disconnect();
    }

    private VirtualMachine connect(int port) throws IOException {
            VirtualMachineManager manager = Bootstrap.virtualMachineManager();

            // Find appropiate connector
            List<AttachingConnector> connectors = manager.attachingConnectors();
            AttachingConnector chosenConnector = null;
            for (AttachingConnector c : connectors) {
                    if (c.transport().name().equals(TRANSPORT_NAME)) {
                            chosenConnector = c;
                            break;
                    }
            }
            if (chosenConnector == null) {
                    throw new IllegalStateException("Could not find socket connector");
            }

            // Set port argument
            AttachingConnector connector = chosenConnector;
            Map<String, Argument> defaults = connector.defaultArguments();
            Argument arg = defaults.get(PORT_ARGUMENT_NAME);
            if (arg == null) {
                    throw new IllegalStateException("Could not find port argument");
            }
            arg.setValue(Integer.toString(port));

            // Attach
            try {
                    System.out.println("Connector arguments: " + defaults);
                    return connector.attach(defaults);
            } catch (IllegalConnectorArgumentsException e) {
                    throw new IllegalArgumentException("Illegal connector arguments", e);
            }
    }

    public void disconnect() {
            if (vm != null) {
                    vm.dispose();
                    vm = null;
            }
    }

    public void redefineClasses(Map<ReferenceType,byte[]> map) {             
        for (ReferenceType refType : map.keySet()) {
            this.logger.log(Level.INFO, "Redefine class " + refType.name());
        }            
        vm.redefineClasses(map);
    }

    /**
     * Call this method before calling allClasses() in order to refresh the JDI state of loaded classes. This is
     * necessary because the JDI map of all loaded classes is only updated based on events received over JDWP (network
     * connection) and therefore it is not necessarily up-to-date with the real state within the VM.
     */
    private void refreshAllClasses() {
            try {
                    Field f = vm.getClass().getDeclaredField("retrievedAllTypes");
                    f.setAccessible(true);
                    f.set(vm, false);
            } catch (IllegalArgumentException ex) {
//                    Logger.getLogger(HotSwapTool.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
//                    Logger.getLogger(HotSwapTool.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchFieldException ex) {
//                    Logger.getLogger(HotSwapTool.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
//                    Logger.getLogger(HotSwapTool.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    private static ReferenceType findReference(List<ReferenceType> list, String name) {
            for (ReferenceType ref : list) {
                    if (ref.name().equals(name)) {
                            return ref;
                    }
            }
            throw new IllegalArgumentException("Cannot find corresponding reference for class name '" + name + "'");
    }
    
}
