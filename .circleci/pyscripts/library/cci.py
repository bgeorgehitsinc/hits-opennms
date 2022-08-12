"""
Main class for representing CircleCI objects
"""
from library.cci_components import workflow
from library import libfile


class cci:

    _workflow_path = None
    _workflow = None

    _file_library = None

    def __init__(self) -> None:
        self._file_library = libfile.libfile()
        self._workflow = workflow.workflow()

    def set_Workflow(self, path) -> bool:
        self._workflow_path = path
        return self._workflow.load(path)

    def get_Workflow_dependency(self, workflow) -> str:
        return self._workflow.get_dependency(workflow)

    def get_Workflow_yaml(
        self, workflow, leadingSpace=0, enable_filters=True, raw_output=True
    ) -> str:
        if raw_output:
            return self._workflow.get_workflow_yaml(
                workflow, leadingSpace, enable_filters
            )
        else:
            return "\n".join(
                self._workflow.get_workflow_yaml(workflow, leadingSpace, enable_filters)
            )
