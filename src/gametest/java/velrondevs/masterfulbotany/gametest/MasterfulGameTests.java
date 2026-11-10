package velrondevs.masterfulbotany.gametest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = "masterfulbotany")
public final class MasterfulGameTests {

    private static final String MOD_ID = "masterfulbotany";

    private static final List<Class<?>> TEST_CLASSES = List.of(
            AlfheimTests.class,
            BlockEntityTests.class,
            EntityTests.class,
            FlowerTests.class,
            ItemTests.class,
            MiscTests.class,
            MjoellnirTests.class,
            RecipeTests.class,
            TerrainTests.class
    );

    private MasterfulGameTests() {}

    private record Entry(Identifier id, Method method, GameTest info) {}

    private static List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        for (Class<?> cls : TEST_CLASSES) {
            for (Method method : cls.getDeclaredMethods()) {
                GameTest info = method.getAnnotation(GameTest.class);
                if (info != null && Modifier.isStatic(method.getModifiers())) {
                    String path = snake(cls.getSimpleName()) + "/" + snake(method.getName());
                    entries.add(new Entry(Identifier.fromNamespaceAndPath(MOD_ID, path), method, info));
                }
            }
        }
        return entries;
    }

    private static String snake(String name) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                builder.append('_');
            }
            builder.append(Character.toLowerCase(c));
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, helper -> {
            for (Entry entry : entries()) {
                Consumer<GameTestHelper> function = test -> {
                    try {
                        entry.method().setAccessible(true);
                        entry.method().invoke(null, test);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        if (e.getCause() instanceof RuntimeException runtime) throw runtime;
                        if (e.getCause() instanceof Error error) throw error;
                        throw new RuntimeException(e.getCause());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                };
                helper.register(entry.id(), function);
            }
        });
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(Identifier.fromNamespaceAndPath(MOD_ID, "default"));
        for (Entry entry : entries()) {
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, entry.id());
            Identifier structure = Identifier.fromNamespaceAndPath(MOD_ID, entry.info().template());
            event.registerTest(entry.id(), new FunctionGameTestInstance(function, new TestData<>(environment, structure, entry.info().timeoutTicks(), 0, true)));
        }
    }
}
