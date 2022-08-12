import re
import os
import shutil

from library import libfile


class common:
    """
    Helper class containing common functionalities
    """
    def __init__(self) -> None:
        self._file_library = libfile.libfile()

    def create_space(self, space):
        """
        Creates number of empty space based on given value
        """
        return " " * space

    def extract_keywords(self, path):
        re_pattern = re.compile("^.*#.*#")
        keywords = {}
        for line in self._file_library.read_file(path):
            re_match = re.match(re_pattern, line)
            if re_match:
                block = re_match.group().split(":")[0].strip().replace("#", "")
                if block not in keywords:
                    keywords[block] = {}
                keywords[block][re_match.group().strip()] = {
                    "block_indentation": len(re.findall(" ", line))
                }
        return keywords

    def expand_index(self, index, path_to_main_folder, tmp_output=[]):
        """
        Reads an index file and expands it
        """
        folder = index.replace("#", "").split(":")[0].strip()
        filepath = index.replace("#", "").split(":")[1].strip()
        file_content = self._file_library.read_file(
            os.path.join(path_to_main_folder, folder, filepath)
        )
        re_pattern = re.compile("^.*#.*#")
        for entry in file_content:
            tmp_match = re.match(re_pattern, entry)
            if tmp_match:
                if ".index" in tmp_match.group().strip():
                    print("We do not support nested index files")
                else:
                    tmp_output.append(
                        self.expand_keyword(
                            tmp_match.group().strip(), path_to_main_folder
                        )
                    )

        shutil.move(
            os.path.join(path_to_main_folder, folder, filepath),
            os.path.join(path_to_main_folder, folder, filepath + "_DONE"),
        )

        return tmp_output

    def expand_keyword(self, index, path_to_main_folder):
        """
        Expands the keywords found in the yaml file
        """
        folder = index.replace("#", "").split(":")[0].strip()
        filepath = index.replace("#", "").split(":")[1].strip() + ".yml"
        file_content = self._file_library.read_file(
            os.path.join(path_to_main_folder, folder, filepath)
        )[1:]

        shutil.move(
            os.path.join(path_to_main_folder, folder, filepath),
            os.path.join(path_to_main_folder, folder, filepath + "_DONE"),
        )

        return file_content
