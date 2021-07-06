package jetty.uber;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;

public class CaseInsensitiveAliasChecker implements ContextHandler.AliasCheck
{
    private static final Logger LOG = Log.getLogger(CaseInsensitiveAliasChecker.class);

    @Override
    public boolean check(String uri, Resource resource)
    {
        // Only support PathResource alias checking
        if (!(resource instanceof PathResource))
            return false;

        try
        {
            PathResource pathResource = (PathResource)resource;
            Path path = pathResource.getPath();
            Path alias = pathResource.getAliasPath();

            if (Files.isSameFile(path, alias) && isCaseInsensitive(path, alias))
            {
                if (LOG.isDebugEnabled())
                    LOG.debug("Allow alias to same file and case insensitive {} --> {}", path, alias);
                return true;
            }
        }
        catch (IOException e)
        {
            LOG.ignore(e);
        }
        return false;
    }

    public static boolean isCaseInsensitive(Path pathA, Path pathB)
    {
        int aCount = pathA.getNameCount();
        int bCount = pathB.getNameCount();
        if (aCount != bCount)
        {
            // different number of segments
            return false;
        }

        // compare each segment of path, backwards, for the same name case insensitive
        for (int i = bCount; i-- > 0; )
        {
            if (!pathA.getName(i).toString().equalsIgnoreCase(pathB.getName(i).toString()))
            {
                return false;
            }
        }

        return true;
    }
}
