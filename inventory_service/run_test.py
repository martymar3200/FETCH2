import subprocess
result = subprocess.run(["poetry", "run", "pytest", "tests/domain/test_location_hierarchy.py::test_shelf_creation_positions", "-v", "-ra", "--tb=short"], capture_output=True, text=True)
print(result.stdout)
print("STDERR", result.stderr)
