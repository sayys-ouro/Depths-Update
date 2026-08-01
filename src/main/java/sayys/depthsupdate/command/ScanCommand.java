package sayys.depthsupdate.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

/**
 * Reports worldgen invariant violations in the chunks around the sender.
 */
public class ScanCommand extends CommandBase {
    private static final int DEFAULT_RADIUS = 5;
    private static final int MAX_RADIUS = 16;
    private static final int EXAMPLES_PER_ISSUE = 5;

    private enum Issue {
        UNSUPPORTED_FLUID("unsupported fluid", "source block with air directly below"),
        EXPOSED_FLUID("exposed fluid", "source block with air horizontally adjacent"),
        STRAY_BEDROCK("stray bedrock", "bedrock above the world floor"),
        ORPHAN_LIGHT("orphan light", "block light exceeding every neighbour");

        private final String label;
        private final String detail;

        Issue(String label, String detail) {
            this.label = label;
            this.detail = detail;
        }
    }

    private static final class Report {
        private final Map<Issue, Integer> counts = new EnumMap<>(Issue.class);
        private final Map<Issue, List<BlockPos>> examples = new EnumMap<>(Issue.class);

        void record(Issue issue, int x, int y, int z) {
            counts.merge(issue, 1, Integer::sum);
            List<BlockPos> found = examples.computeIfAbsent(issue, key -> new ArrayList<>());

            if (found.size() < EXAMPLES_PER_ISSUE) {
                found.add(new BlockPos(x, y, z));
            }
        }

        int count(Issue issue) {
            return counts.getOrDefault(issue, 0);
        }

        List<BlockPos> examples(Issue issue) {
            return examples.getOrDefault(issue, Collections.emptyList());
        }
    }

    @Override
    public String getName() {
        return "depthsupdate";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("du");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/depthsupdate scan [radius in chunks, 0-" + MAX_RADIUS + "]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "scan") : Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || !"scan".equalsIgnoreCase(args[0])) {
            throw new WrongUsageException(getUsage(sender));
        }

        int radius = args.length > 1 ? parseInt(args[1], 0, MAX_RADIUS) : DEFAULT_RADIUS;
        World world = sender.getEntityWorld();
        HeightContext ctx = HeightManager.get(world);
        BlockPos origin = sender.getPosition();

        int minY = ctx.minY();
        int maxY = Math.min(ctx.seaLevel(), ctx.maxY() - 1);
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;

        Report report = new Report();
        long started = System.nanoTime();
        int chunksScanned = 0;

        for (int cx = originChunkX - radius; cx <= originChunkX + radius; cx++) {
            for (int cz = originChunkZ - radius; cz <= originChunkZ + radius; cz++) {
                if (!world.isBlockLoaded(new BlockPos((cx << 4) + 8, minY, (cz << 4) + 8))) {
                    continue;
                }

                scanChunk(world, world.getChunk(cx, cz), cx, cz, minY, maxY, report);
                chunksScanned++;
            }
        }

        long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
        long blocks = (long) chunksScanned * 256L * (maxY - minY + 1);

        sender.sendMessage(new TextComponentString(String.format(
                "%sDepths Update scan%s: %d chunks, Y %d..%d (%,d blocks) in %d ms",
                TextFormatting.AQUA, TextFormatting.RESET, chunksScanned, minY, maxY, blocks, elapsedMs)));

        if (chunksScanned == 0) {
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  no loaded chunks in range"));

            return;
        }

        for (Issue issue : Issue.values()) {
            int count = report.count(issue);
            TextFormatting colour = count == 0 ? TextFormatting.GREEN : TextFormatting.RED;
            StringBuilder line = new StringBuilder(String.format("  %s%-18s %5d%s", colour, issue.label, count, TextFormatting.RESET));

            if (count == 0) {
                line.append(TextFormatting.DARK_GRAY).append("  (").append(issue.detail).append(')');
            } else {
                line.append(TextFormatting.GRAY).append("  e.g. ");

                List<BlockPos> examples = report.examples(issue);

                for (int i = 0; i < examples.size(); i++) {
                    BlockPos pos = examples.get(i);

                    if (i > 0) {
                        line.append(", ");
                    }

                    line.append(pos.getX()).append(' ').append(pos.getY()).append(' ').append(pos.getZ());
                }

                if (count > examples.size()) {
                    line.append(String.format(" (+%d more)", count - examples.size()));
                }
            }

            sender.sendMessage(new TextComponentString(line.toString()));
        }
    }

    private void scanChunk(World world, Chunk chunk, int chunkX, int chunkZ, int minY, int maxY, Report report) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int bedrockCeiling = minY + 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = baseX + dx;
                int z = baseZ + dz;

                for (int y = minY; y <= maxY; y++) {
                    pos.setPos(x, y, z);

                    IBlockState state = chunk.getBlockState(pos);

                    if (state.getBlock() == Blocks.BEDROCK) {
                        if (y > bedrockCeiling) {
                            report.record(Issue.STRAY_BEDROCK, x, y, z);
                        }

                        continue;
                    }

                    if (state.getBlock() instanceof BlockLiquid && state.getValue(BlockLiquid.LEVEL) == 0) {
                        checkFluidSupport(world, pos, x, y, z, minY, report);
                    }

                    checkLight(world, chunk, pos, state, x, y, z, minY, maxY, report);
                }
            }
        }
    }

    private void checkFluidSupport(World world, BlockPos.MutableBlockPos pos, int x, int y, int z, int minY, Report report) {
        if (y - 1 >= minY) {
            pos.setPos(x, y - 1, z);

            if (world.isAirBlock(pos)) {
                report.record(Issue.UNSUPPORTED_FLUID, x, y, z);
                pos.setPos(x, y, z);

                return;
            }
        }

        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            pos.setPos(x + facing.getXOffset(), y, z + facing.getZOffset());

            if (world.isBlockLoaded(pos) && world.isAirBlock(pos)) {
                report.record(Issue.EXPOSED_FLUID, x, y, z);
                break;
            }
        }

        pos.setPos(x, y, z);
    }

    private void checkLight(World world, Chunk chunk, BlockPos.MutableBlockPos pos, IBlockState state,
            int x, int y, int z, int minY, int maxY, Report report) {
        int light = chunk.getLightFor(EnumSkyBlock.BLOCK, pos);

        if (light <= 0 || state.getLightValue(world, pos) >= light) {
            return;
        }

        int brightestNeighbour = 0;

        for (EnumFacing facing : EnumFacing.VALUES) {
            int ny = y + facing.getYOffset();

            // A neighbour past the world edge cannot be read, and one past the
            // scanned band may legitimately be the brighter source.
            if (ny < minY || ny > maxY) {
                return;
            }

            pos.setPos(x + facing.getXOffset(), ny, z + facing.getZOffset());

            if (!world.isBlockLoaded(pos)) {
                pos.setPos(x, y, z);

                return;
            }

            brightestNeighbour = Math.max(brightestNeighbour, world.getLightFor(EnumSkyBlock.BLOCK, pos));
        }

        pos.setPos(x, y, z);

        if (light >= brightestNeighbour) {
            report.record(Issue.ORPHAN_LIGHT, x, y, z);
        }
    }
}
