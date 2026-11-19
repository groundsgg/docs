"""Compile the Java/Kotlin examples that readers actually copy from the NPC guides."""
from pathlib import Path
import re
import sys

repo = Path(__file__).resolve().parents[1]
output = Path(sys.argv[1])
contexts = {
    'instance': ('net.minestom.server.instance.Instance', 'net.minestom.server.instance.Instance'),
    'npc': ('gg.grounds.npc.minestom.NpcHandle', 'gg.grounds.npc.minestom.NpcHandle'),
    'firstNpc': ('example.npc.FirstNpc', 'example.npc.FirstNpc'),
    'animations': ('example.npc.NpcAnimations', 'example.npc.NpcAnimations'),
    'player': ('net.minestom.server.entity.Player', 'net.minestom.server.entity.Player'),
    'destination': ('gg.grounds.npc.minestom.transform.NpcRenderTransform', 'gg.grounds.npc.minestom.transform.NpcRenderTransform'),
}
for language, extension in [('java', 'java'), ('kotlin', 'kt')]:
    methods = []
    imports = set()
    for page in sorted((repo / 'reference/libraries/npc').glob('*.mdx')):
        blocks = re.findall(r'^```' + language + r'(?: ([^\n]*))?\n(.*?)^```\s*$', page.read_text(), re.M | re.S)
        for label, block in blocks:
            if label == 'build.gradle.kts':
                continue
            body = []
            for line in block.strip().splitlines():
                if line.startswith('import '):
                    imports.add(line.rstrip(';'))
                else:
                    body.append(line)
            code = '\n'.join(body)
            number = len(methods)
            if language == 'java':
                methods.append('  // ' + page.name + '\n  static void snippet' + str(number) + '() throws Exception {\n' + code + '\n  }')
            else:
                methods.append('// ' + page.name + '\nfun snippet' + str(number) + '() {\n' + code + '\n}')
    directory = output / language / 'docscheck'
    directory.mkdir(parents=True, exist_ok=True)
    prefix = 'package docscheck;\n' if language == 'java' else 'package docscheck\n'
    prefix += '\n'.join(line + (';' if language == 'java' else '') for line in sorted(imports)) + '\n'
    fields = '\n'.join(('  static ' + types[0] + ' ' + name + ';') if language == 'java' else ('lateinit var ' + name + ': ' + types[1]) for name, types in contexts.items())
    body = 'public final class JavaGuideSnippets {\n' + fields + '\n' + '\n'.join(methods) + '\n}' if language == 'java' else fields + '\n' + '\n'.join(methods)
    (directory / ('JavaGuideSnippets.java' if language == 'java' else 'KotlinGuideSnippets.kt')).write_text(prefix + body + '\n')
    print(f'{language}: {len(methods)} guide snippets extracted')
