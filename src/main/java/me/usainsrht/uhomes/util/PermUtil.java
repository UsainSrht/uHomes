package me.usainsrht.uhomes.util;

import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashSet;
import java.util.Set;

public class PermUtil {

    public static int getMetaLimit(String key, User user) {
        int limit = 0;

        if (user == null) return limit;

        Set<PermissionHolder> permissionHolders = new HashSet<>();
        permissionHolders.add(user);
        permissionHolders.addAll(user.getInheritedGroups(QueryOptions.defaultContextualOptions()));

        for (PermissionHolder permissionHolder : permissionHolders) {
            for (MetaNode node : permissionHolder.getNodes(NodeType.META)) {
                if (node.getMetaKey().equals(key)) {
                    limit += Integer.parseInt(node.getMetaValue());
                    break;
                }
            }
        }

        return limit;
    }

    public static int getSummedLimit(String permission, Permissible permissible) {
        int total = 0;

        if (permissible == null) return total;

        for (PermissionAttachmentInfo permInfo : permissible.getEffectivePermissions()) {
            String currentPermission = permInfo.getPermission();
            if (!currentPermission.startsWith(permission)) continue;
            String substr = currentPermission.substring(permission.length());
            try {
                int number = Integer.parseInt(substr);
                total += number;
            } catch (NumberFormatException ignore) {}
        }
        return total;
    }

    public static int getHighestLimit(String permission, Permissible permissible) {
        int highest = 0;

        if (permissible == null) return highest;

        for (PermissionAttachmentInfo permInfo : permissible.getEffectivePermissions()) {
            String currentPermission = permInfo.getPermission();
            if (!currentPermission.startsWith(permission)) continue;
            String substr = currentPermission.substring(permission.length());
            try {
                int number = Integer.parseInt(substr);
                if (number > highest) highest = number;
            } catch (NumberFormatException ignore) {}
        }
        return highest;
    }




}
